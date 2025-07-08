package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.MissingComponentAgent
import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.NodeRelation
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
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val uiGraphService: UiGraphService
) {
    private val log = logger()

    fun initializeGraph(kioskId: String, menuList: List<MenuInfoDto>): Pair<List<AgentActionDto>, UiEntity> {
        log.info("메뉴 utg 생성 시작")
        val startTime = System.nanoTime()

        var isNext = true
        val history = mutableListOf<AgentActionDto>()

        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "root"
        )
        var preNode = uiGraphService.saveNode(rootUiDto)
        var llmUiList = mutableListOf<LlmUiComponentDto>()
        var menuIndex = 0
        while (true) {
            val menuDto = menuList[menuIndex]

            //이전 카테고리랑 다르면 이미지 파싱
            if (isNext == true) {
                llmUiList.clear()
                val image = notificationService.sendCaptureCommand(kioskId)
                llmUiList = uiExtractorManager.getUiComponents(image, kioskId)
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
            val entity = uiGraphService.saveNode(UiDto(
                isNext = isNext,
                x = action.coordinate[0], y = action.coordinate[1],
                title = action.title,
                kioskId = kioskId
            ))

            //sse를 통해 클라이언트에게 현재 메뉴 좌표 클릭하도록 하기
            notificationService.sendActionCommand(kioskId, action.coordinate)

            //옵션 노드 초기화
            processOptions(
                menuDto = menuDto,
                entity = entity, preNode = preNode,
                kioskId = kioskId
            )

            //isNext이고 이전이랑 카테고리 다른 경우. 다른 페이지로 가야하는 경우
            if (isNext) {
                uiGraphService.saveRel(preNode.id, entity.id, NodeRelation.PATH_TO)
                uiGraphService.saveRel(entity.id, preNode.id, NodeRelation.PATH_TO)
                preNode = entity
            } else {
                uiGraphService.saveRel(preNode.id, entity.id, NodeRelation.HAS_TO)
                menuIndex++
            }

            if (menuIndex == menuList.size) break
        }

        val endTime = System.nanoTime()
        log.info("메뉴 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")

        return Pair(history, preNode)
    }

    private fun processOptions(
        menuDto: MenuInfoDto,
        entity: UiEntity,
        preNode: UiEntity,
        kioskId: String
    ): AgentBackDto {
        //메뉴의 옵션 노드 추가
        val image = notificationService.sendCaptureCommand(kioskId)
        val llmOptList = uiExtractorManager.getUiComponents(image, kioskId)

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

            log.info("옵션 노드를 생성합니다. go_next: ${optAction.goNext}, score: ${optAction.score}, coordinate: ${optAction.coordinate}, title: ${optAction.title}")
            val optEntity = uiGraphService.saveNode(UiDto(
                isNext = optAction.goNext,
                x = optAction.coordinate[0], y = optAction.coordinate[1],
                title = optAction.title,
                kioskId = kioskId
            ))
            uiGraphService.saveRel(entity.id, optEntity.id, NodeRelation.OPT_TO)
        }

        val backEntity = uiGraphService.saveNode(UiDto(
            isNext = false,
            x = backAction.coordinate[0], y = backAction.coordinate[1],
            title = backAction.title,
            kioskId = kioskId
        ))
        uiGraphService.saveRel(entity.id, backEntity.id, NodeRelation.BACK_TO)
        uiGraphService.saveRel(backEntity.id, preNode.id, NodeRelation.BACK_TO)

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        log.info("돌아가는 좌표를 클릭중입니다. 좌표: ${backAction.coordinate}")
        notificationService.sendActionCommand(kioskId, backAction.coordinate)

        return backAction
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