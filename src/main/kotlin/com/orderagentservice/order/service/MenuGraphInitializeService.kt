package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.MissingComponentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.exception.LowScoreException
import com.orderagentservice.order.model.GraphInitializeContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuGraphDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    @Transactional
    fun initializeGraph(context: GraphInitializeContext, menuList: List<MenuInfoDto>) {
        log.info("메뉴 utg 생성 시작")
        val startTime = System.nanoTime()

        var isNext = true
        var isFirst = true
        val kioskId = context.kioskId

        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "root"
        )
        context.lastNode = utgService.saveNode(rootUiDto)
        var llmUiList = mutableListOf<LlmUiComponentDto>()
        var menuIndex = 0
        while (menuIndex < menuList.size) {
            val menuDto = menuList[menuIndex]

            //이전 카테고리랑 다르면 이미지 파싱
            if (isNext == true) {
                val image = notificationService.sendCaptureCommand(kioskId)
                llmUiList = uiExtractorManager.getUiComponents(image, kioskId)

                //첫 노드인 경우 따로 처리
                if (isFirst == true) {
                    handleFirstNode(context, menuDto, llmUiList)
                    isFirst = false
                }
                isNext = false
            } else {
                val isProcessed = processMenu(context, menuDto, llmUiList)

                //다음 페이지로 넘어가야 함
                if (isProcessed == true) {
                    isNext = true
                } else {
                    menuIndex++
                }
            }
        }

        //포장 매장 확인
        if (context.isFindPlace == false) {
            placeGraphInitializeService.initializeGraph(context, llmUiList)
        }

        val endTime = System.nanoTime()
        log.info("메뉴 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    private fun handleFirstNode(context: GraphInitializeContext, firstMenu: MenuInfoDto, llmUiList: List<LlmUiComponentDto>) {
        // 포장/매장 UI 확인
        placeGraphInitializeService.initializeGraph(context, llmUiList)

        // 루트 -> 첫 카테고리 노드로 연결
        val categoryMenuDto = MenuInfoDto(title = firstMenu.category, options = listOf(), category = firstMenu.category)
        val categoryAction = menuAgent.determineAction(categoryMenuDto, llmUiList)
        val categoryNode = utgService.saveNode(UiDto(
            isNext = true,
            x = categoryAction.coordinate[0], y = categoryAction.coordinate[1],
            title = categoryAction.title,
            kioskId = context.kioskId
        ))

        utgService.saveRel(context.lastNode!!.id, categoryNode.id, NodeRelation.PATH_TO)
        context.lastNode = categoryNode
        context.history.add(categoryAction)
    }

    private fun processMenu(context: GraphInitializeContext, menuDto: MenuInfoDto, llmUiList: List<LlmUiComponentDto>): Boolean {
        log.info("진행 중인 메뉴: ${menuDto.title}, 카테고리: ${menuDto.category}")

        val action = menuAgent.determineAction(menuDto, llmUiList)
        context.history.add(action)

        val addCount = when {
            action.score in 0.6..<0.7 -> 1
            action.score <= 0.5 -> 3
            else -> 0
        }

        if (addCount > 0) {
            log.info("낮은 액션 정확도 점수: ${action.score}. 카운트를 추가하고 다음 메뉴로 넘어갑니다.")
            context.lowScoreCount += addCount
            if (context.lowScoreCount >= 5) {
                throw LowScoreException()
            }
            return false // 현재 페이지에 머무름
        } else {
            log.info("메뉴 노드를 생성합니다. go_next: ${action.goNext}, score: ${action.score}, title: ${action.title}")
            val node = utgService.saveNode(UiDto(
                isNext = action.goNext,
                x = action.coordinate[0], y = action.coordinate[1],
                title = action.title,
                kioskId = context.kioskId
            ))

            // SSE를 통해 클라이언트에게 현재 메뉴 좌표 클릭하도록 하기
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(action.coordinate[0], action.coordinate[1], action.title))

            // 옵션 노드 초기화
            processOptions(menuDto, context)

            // 페이지 이동 여부에 따라 관계 설정
            if (action.goNext) {
                utgService.saveRel(context.lastNode!!.id, node.id, NodeRelation.PATH_TO)
                utgService.saveRel(node.id, context.lastNode!!.id, NodeRelation.PATH_TO)
                context.lastNode = node
                return true // 다음 페이지로 이동
            } else {
                utgService.saveRel(context.lastNode!!.id, node.id, NodeRelation.HAS_TO)
                return false // 현재 페이지에 머무름
            }
        }
    }

    private fun processOptions(
        menuDto: MenuInfoDto,
        context: GraphInitializeContext
    ) {
        //메뉴의 옵션 노드 추가
        val kioskId = context.kioskId
        val image = notificationService.sendCaptureCommand(kioskId)
        val llmOptList = uiExtractorManager.getUiComponents(image, kioskId)

        //TODO(GPT 미신청으로 인한 일시적인 보류)
//        //감지되지 못한 옵션이 있을 수 있으므로 한번 더 탐색
//        val additionalList = missingComponentAgent.determineAction(image, menuDto.options, llmOptList)
//
//        //추가된거랑 겹치는게 있다는 말은 + 버튼이 아닌 것이므로 지워줘야함
//        removeDuplicate(additionalList, llmOptList)
//        for (addEle in additionalList) {
//            llmOptList.add(addEle)
//        }

        //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
        val backAction = backAgent.determineBack(llmOptList)
        for (opt in menuDto.options) {
            val optAction = menuAgent.determineAction(MenuInfoDto(opt, listOf(), menuDto.title), llmOptList)
            context.history.add(optAction)

            log.info("옵션 노드를 생성합니다. go_next: ${optAction.goNext}, score: ${optAction.score}, coordinate: ${optAction.coordinate}, title: ${optAction.title}")
            val optEntity = utgService.saveNode(UiDto(
                isNext = optAction.goNext,
                x = optAction.coordinate[0], y = optAction.coordinate[1],
                title = optAction.title,
                kioskId = kioskId
            ))
            utgService.saveRel(context.lastNode!!.id, optEntity.id, NodeRelation.OPT_TO)
        }

        //옵선을 선택해야

        val backEntity = utgService.saveNode(UiDto(
            isNext = false,
            x = backAction.coordinate[0], y = backAction.coordinate[1],
            title = backAction.title,
            kioskId = kioskId
        ))
        utgService.saveRel(context.lastNode!!.id, backEntity.id, NodeRelation.BACK_TO)
        utgService.saveRel(backEntity.id, context.lastNode!!.id, NodeRelation.BACK_TO)

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        log.info("돌아가는 좌표를 클릭중입니다. 좌표: ${backAction.coordinate}")
        notificationService.sendActionCommand(kioskId,  CoordinateDto(backAction.coordinate[0], backAction.coordinate[1], backAction.title))

        context.history.add(backAction.toActionDto())
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

//메뉴는 모달이 뜰 수도 있음
//모달이 있는 경우 모달에서 메뉴를 누르고 또 완료를 눌러야함
//메뉴클릭 -> 메뉴클릭 -> 완료 이 순서임
//그러나 메뉴 두번클릭까지는 다른 키오스크에서 문제 없는데 마지막 완료는 다른 키오스크에서 문제가 발생함

//즉, 메뉴를 클릭하고 옵션이 안나오면 완료를 누르고 옵션이 나오면 정상적으로 진행
//옵션이 나오는지는 메뉴클릭 -> 옴니파서 -> 메타를 순회하며 옵션에 해당하는 단어가 있는 지 확인 (이때 메타는 오타가 많을 수 있으므로 agent이용)
//모달은 뜨는데 옵션이 없는 경우가 있음. 이 경우 메뉴클릭 -> 메뉴클릭 -> 완료
//메뉴는 모두 (메뉴클릭 -> 옴니파서 -> 메타검사 -> 메뉴클릭 -> 완료 -> 옵션)로 가야하고 유일하게 고려해야할 점은 모달이 안뜨는 경우
//                                    ㄴ-> 옵션
//모달이 없는 경우도 검사를 해야함. 즉 클릭하고 메타가 바뀌지 않았다면 모달이 없는 것임. 이는 캡쳐이미지 해싱을 통해 알아볼 수 있음.


//옵션까지 선택하고 카드에 담기 전에 완료를 한번 더 눌러야할 수 있음
//이것도 미리 메뉴창 이미지를 해싱해 두었다가 완료를 계속누르고 해싱한 이미지와 같아지면 그만