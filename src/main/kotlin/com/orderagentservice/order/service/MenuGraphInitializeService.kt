package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.MissingComponentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuGraphDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MenuGraphInitializeService @Autowired constructor(
    private val menuAgent: MenuAgent,
    private val backAgent: BackAgent,
    private val missingComponentAgent: MissingComponentAgent,
    private val placeGraphInitializeService: PlaceGraphInitializeService,
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val utgService: UtgService
) {
    private val log = logger()

    fun initializeGraph(kioskId: String, menuList: List<MenuInfoDto>): MenuGraphDto {
        log.info("메뉴 utg 생성 시작")
        val startTime = System.nanoTime()

        var isNext = true
        var isFirst = true
        var isFindPlace = false
        val history = mutableListOf<AgentActionDto>()

        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "root"
        )
        var preNode = utgService.saveNode(rootUiDto)
        var llmUiList = mutableListOf<LlmUiComponentDto>()
        var menuIndex = 0
        while (true) {
            val menuDto = menuList[menuIndex]

            //이전 카테고리랑 다르면 이미지 파싱
            if (isNext == true) {
                llmUiList.clear()
                val image = notificationService.sendCaptureCommand(kioskId)
                llmUiList = uiExtractorManager.getUiComponents(image, kioskId)

                if (isFirst == true) {
                    //포장/매장 UI 확인
                    placeGraphInitializeService.initializeGraph(kioskId, preNode, llmUiList)
                        .onEach { history.add(it) }
                        .also { list -> if (list.size == 2) isFindPlace = true }

                    //루트 -> 처음 카테고리 노드로 연결
                    val firstMenuDto = MenuInfoDto(title = menuDto.category, options = listOf(), category = menuDto.category)
                    val firstAction = menuAgent.determineAction(firstMenuDto, llmUiList)
                    val firstNode = utgService.saveNode(UiDto(
                        isNext = true,
                        x = firstAction.coordinate[0], y = firstAction.coordinate[1],
                        title = firstAction.title,
                        kioskId = kioskId
                    ))

                    utgService.saveRel(preNode.id, firstNode.id, NodeRelation.PATH_TO)
                    preNode = firstNode

                    history.add(firstAction)
                    isFirst = false
                }

                isNext = false
                continue
            }

            log.info("진행 중인 노드 menu: ${menuDto.title}, category: ${menuDto.category}")

            val action = menuAgent.determineAction(menuDto, llmUiList)
            history.add(action)

            if (action.score < 0.6) {
                log.info("낮은 액션 정확도 점수: ${action.score}")
                continue
            }

            log.info("메뉴 노드를 생성합니다. go_next: ${action.goNext}, score: ${action.score}, coordinate: ${action.coordinate}, title: ${action.title}")
            isNext = action.goNext
            val node = utgService.saveNode(UiDto(
                isNext = isNext,
                x = action.coordinate[0], y = action.coordinate[1],
                title = action.title,
                kioskId = kioskId
            ))

            //sse를 통해 클라이언트에게 현재 메뉴 좌표 클릭하도록 하기
            notificationService.sendActionCommand(kioskId,  CoordinateDto(action.coordinate[0], action.coordinate[1], action.title))

            //옵션 노드 초기화
            processOptions(
                menuDto = menuDto,
                entity = node, preNode = preNode,
                kioskId = kioskId
            ).forEach { history.add(it) }

            //isNext이고 이전이랑 카테고리 다른 경우. 다른 페이지로 가야하는 경우
            if (isNext) {
                utgService.saveRel(preNode.id, node.id, NodeRelation.PATH_TO)
                utgService.saveRel(node.id, preNode.id, NodeRelation.PATH_TO)
                preNode = node
            } else {
                utgService.saveRel(preNode.id, node.id, NodeRelation.HAS_TO)
                menuIndex++
            }

            if (menuIndex == menuList.size) break
        }

        //포장 매장 확인
        if (isFindPlace == false) {
            placeGraphInitializeService.initializeGraph(kioskId, preNode, llmUiList)
                .onEach { history.add(it) }
                .also { list -> if (list.size == 2) isFindPlace = true }
        }

        val endTime = System.nanoTime()
        log.info("메뉴 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")

        return MenuGraphDto(history, preNode, isFindPlace)
    }

    private fun processOptions(
        menuDto: MenuInfoDto,
        entity: UiEntity,
        preNode: UiEntity,
        kioskId: String
    ): List<AgentActionDto> {
        //메뉴의 옵션 노드 추가
        val image = notificationService.sendCaptureCommand(kioskId)
        val llmOptList = uiExtractorManager.getUiComponents(image, kioskId)
        val optActionList = mutableListOf<AgentActionDto>()

        //감지되지 못한 옵션이 있을 수 있으므로 한번 더 탐색
        val additionalList = missingComponentAgent.determineAction(image, menuDto.options, llmOptList)

        //추가된거랑 겹치는게 있다는 말은 + 버튼이 아닌 것이므로 지워줘야함
        removeDuplicate(additionalList, llmOptList)
        for (addEle in additionalList) {
            llmOptList.add(addEle)
        }

        //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
        val backAction = backAgent.determineBack(llmOptList)
        for (opt in menuDto.options) {
            val optAction = menuAgent.determineAction(MenuInfoDto(opt, listOf(), menuDto.title), llmOptList)
            optActionList.add(optAction)

            log.info("옵션 노드를 생성합니다. go_next: ${optAction.goNext}, score: ${optAction.score}, coordinate: ${optAction.coordinate}, title: ${optAction.title}")
            val optEntity = utgService.saveNode(UiDto(
                isNext = optAction.goNext,
                x = optAction.coordinate[0], y = optAction.coordinate[1],
                title = optAction.title,
                kioskId = kioskId
            ))
            utgService.saveRel(entity.id, optEntity.id, NodeRelation.OPT_TO)
        }

        val backEntity = utgService.saveNode(UiDto(
            isNext = false,
            x = backAction.coordinate[0], y = backAction.coordinate[1],
            title = backAction.title,
            kioskId = kioskId
        ))
        utgService.saveRel(entity.id, backEntity.id, NodeRelation.BACK_TO)
        utgService.saveRel(backEntity.id, preNode.id, NodeRelation.BACK_TO)

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        log.info("돌아가는 좌표를 클릭중입니다. 좌표: ${backAction.coordinate}")
        notificationService.sendActionCommand(kioskId,  CoordinateDto(backAction.coordinate[0], backAction.coordinate[1], backAction.title))

        optActionList.add(backAction.toActionDto())
        return optActionList
    }

    private fun removeDuplicate(sourceList: List<LlmUiComponentDto>, targetList: MutableList<LlmUiComponentDto>) {
        for (addEle in sourceList) {
            val stripAddEle = addEle.title.replace(" ", "")
            for (ele in targetList) {
                val stripEle = ele.title.replace(" ", "")
                if (stripAddEle.contains(stripEle)) {
                    targetList.remove(ele)
                }
            }
        }
    }
}