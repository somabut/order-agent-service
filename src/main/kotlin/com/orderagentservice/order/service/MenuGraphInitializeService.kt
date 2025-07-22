package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.MissingComponentAgent
import com.orderagentservice.agent.PageAgent
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.exception.LowScoreException
import com.orderagentservice.order.model.GraphInitializeContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.util.ImageUtils
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenuGraphInitializeService @Autowired constructor(
    private val menuAgent: MenuAgent,
    private val backAgent: BackAgent,
    private val pageAgent: PageAgent,
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
                context.imageHash = ImageUtils.imageToHash(image)

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
        val kioskId = context.kioskId
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

            //현재 메뉴 좌표 클릭
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(action.coordinate[0], action.coordinate[1], action.title))

            //모달 처리
            handelModal(
                menuDto = menuDto,
                menuNode = node,
                context = context
            )

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

    private fun handelModal(menuDto: MenuInfoDto, menuNode: UiEntity, context: GraphInitializeContext) {
        val kioskId = context.kioskId

        val image = notificationService.sendCaptureCommand(kioskId)
        val modalLlmUiList = uiExtractorManager.getUiComponents(image, kioskId)
        val lastNode = context.lastNode

        //여기서 발생한 옴니파서 데이터를 바탕으로 모달 클릭해야함
        val pageAction = pageAgent.determineAction(menuDto.options, modalLlmUiList)
        if (pageAction.contain == false) {
            log.info("모달로 집입합니다")
            //모달이 있는 경우 메뉴 다시 찾아서 클릭
            val modalSelectAction = menuAgent.determineAction(menuDto, modalLlmUiList)
            notificationService.sendActionCommand(kioskId, CoordinateDto(modalSelectAction.coordinate[0], modalSelectAction.coordinate[1], modalSelectAction.title))
            val modalNode = utgService.saveNode(UiDto(
                isNext = modalSelectAction.goNext,
                x = modalSelectAction.coordinate[0], y = modalSelectAction.coordinate[1],
                title = modalSelectAction.title,
                kioskId = context.kioskId
            ))
            utgService.saveRel(menuNode.id, modalNode.id, NodeRelation.PATH_TO)

            //완료를 눌러 옵션으로 이동
            val modalCompleteAction = backAgent.determineBack(modalLlmUiList)
            notificationService.sendActionCommand(kioskId, CoordinateDto(modalCompleteAction.coordinate[0], modalCompleteAction.coordinate[1], modalCompleteAction.title))
            val backNode = utgService.saveNode(UiDto(
                isNext = false,
                x = modalCompleteAction.coordinate[0], y = modalCompleteAction.coordinate[1],
                title = modalCompleteAction.title,
                kioskId = context.kioskId
            ))
            utgService.saveRel(modalNode.id, backNode.id, NodeRelation.BACK_TO)

            //옵션이 있으면 back에서 옵션으로, 옵션이 없다면 원래 노드로 관계
            if (menuDto.options.size > 0) {
                context.lastNode = modalNode
            } else {
                utgService.saveRel(backNode.id, menuNode.id, NodeRelation.BACK_TO)
            }
        }

        //옵션 노드 초기화
        if (menuDto.options.size > 0) {
            processOptions(menuDto, context)
        }

        //원래 메뉴 노드로 복귀
        if (pageAction.contain == false) {
            //되돌아 오는 관계 추가
            utgService.saveRel(context.lastNode!!.id, lastNode!!.id, NodeRelation.BACK_TO)
            context.lastNode = lastNode
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

        var startNode = context.lastNode
        var targetNode: UiEntity?
        //원래 페이지가 나올 때까지 돌아가는 UI 클릭
        do {
            //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
            val backAction = backAgent.determineBack(llmOptList)
            val backEntity = utgService.saveNode(UiDto(
                isNext = false,
                x = backAction.coordinate[0], y = backAction.coordinate[1],
                title = backAction.title,
                kioskId = kioskId
            ))
            targetNode = backEntity

            utgService.saveRel(startNode!!.id, targetNode.id, NodeRelation.BACK_TO)

            //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
            log.info("돌아가는 좌표를 클릭중입니다. 좌표: ${backAction.coordinate}")
            notificationService.sendActionCommand(kioskId, CoordinateDto(backAction.coordinate[0], backAction.coordinate[1], backAction.title))
            context.history.add(backAction.toActionDto())

            //현재 페이지의 해시를 추출하여 메뉴화면으로 빠져나왔는지 확인
            val nowPage = notificationService.sendCaptureCommand(kioskId)
            val hash = ImageUtils.imageToHash(nowPage)

            startNode = targetNode
        } while (hash != context.imageHash)
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