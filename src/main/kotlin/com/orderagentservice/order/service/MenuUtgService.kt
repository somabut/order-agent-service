package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.global.service.WordSimilarityService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenuUtgService @Autowired constructor(
    private val placeUtgService: PlaceUtgService,
    private val backAgent: BackAgent,
    private val wordSimilarityService: WordSimilarityService,
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val graphService: GraphService
) : AbstractMenuUtgService(
    backAgent = backAgent,
    wordSimilarityService = wordSimilarityService,
    uiExtractorManager = uiExtractorManager,
    notificationService = notificationService,
    graphService = graphService
) {
    private val log = logger()

    private val MAX_MODAL_TO_MENU_COUNT = 3

    @Transactional
    fun initializeGraph(context: GraphContext, menuList: List<MenuInfoDto>) {
        log.info("메뉴 utg 생성 시작")
        val startTime = System.nanoTime()

        // root, station노드 초기화
        setupNode(context)

        //포장/매장 찾기
        placeUtgService.initializeGraph(context)

        //루프를 돌며 메뉴들을 모두 탐색
        navigateMenus(context, menuList)

        //포장/매장 찾기
        if (context.isPlaceDetermined == false) {
            placeUtgService.initializeGraph(context)
        }

        val endTime = System.nanoTime()
        log.info("메뉴 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    @Transactional
    fun updateGraph(context: GraphContext, categoryMenuList: List<MenuInfoDto>) {
        log.info("메뉴 utg 업데이트 시작")
        val startTime = System.nanoTime()

        //카테고리로 이동
        moveCategory(context, categoryMenuList)

        //해당 페이지에서 메뉴 탐색
        navigateMenus(context, categoryMenuList)

        val endTime = System.nanoTime()
        log.info("메뉴 utg 업데이트 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    private fun moveCategory(context: GraphContext, categoryMenuList: List<MenuInfoDto>) {
        val nowNodeId = graphService.findRoot(context.kioskId).id
        val category = categoryMenuList[0].category

        //변경이 일어난 카테고리까지 이동
        val actionList = graphService.findMenuPath(context.kioskId, nowNodeId, category)
        for (act in actionList) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(act.x, act.y, act.title))
        }

        //해당 카테고리의 메뉴 제거
        val categoryId = actionList.last().id
        graphService.deleteMenusByCategory(context.kioskId, categoryId)
    }

    private fun setupNode(context: GraphContext) {
        log.info("root노드와 station노드를 초기화합니다.")
        val kioskId = context.kioskId
        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "root"
        )
        context.lastNodeId = graphService.saveNode(rootUiDto).id

        val stationNode = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "station"
        )
        context.stationNodeId = graphService.saveNode(stationNode).id

        graphService.saveRel(context.lastNodeId!!, context.stationNodeId!!, NodeRelation.PATH_TO)
    }

    //TODO(모달 로직 임시 비활성화)
//    private fun handleModal(menuDto: MenuInfoDto, menuNode: UiEntity, context: GraphInitializeContext) {
//        val kioskId = context.kioskId
//
//        val modalLlmUiList = uiExtractorManager.getUiComponents(kioskId)
//        val lastNode = context.lastNode
//
//        //여기서 발생한 옴니파서 데이터를 바탕으로 모달 클릭해야함
//        val pageAction = pageAgent.determineAction(menuDto.options, modalLlmUiList)
//        if (pageAction.contain == false) {
//            //모달이 있는 경우 메뉴 다시 찾아서 클릭
//            log.info("모달로 집입합니다")
//            navigateModal(
//                context = context,
//                menuNode = menuNode,
//                menuDto = menuDto,
//                modalLlmUiList = modalLlmUiList
//            )
//        }
//
//        //옵션 노드 초기화
//        if (menuDto.options.size > 0) {
//            selectOption(menuDto, context)
//        }
//
//        //원래 메뉴 노드로 복귀
//        if (pageAction.contain == false) {
//            //되돌아 오는 관계 추가
//            utgService.saveRel(context.lastNode!!.id, lastNode!!.id, NodeRelation.BACK_TO)
//            context.lastNode = lastNode
//        }
//    }
//
//    private fun navigateModal(context: GraphInitializeContext, menuDto: MenuInfoDto, menuNode: UiEntity, modalLlmUiList: List<LlmUiComponentDto>) {
//        val kioskId = context.kioskId
//
//        val modalSelectAction = menuAgent.determineAction(menuDto, modalLlmUiList)
//        log.info("모달 내부에서 처리중입니다. go_next: ${modalSelectAction.goNext}, score: ${modalSelectAction.score}, title: ${modalSelectAction.title}")
//        context.history.add(modalSelectAction)
//
//        notificationService.sendActionCommand(kioskId, CoordinateDto(modalSelectAction.coordinate[0], modalSelectAction.coordinate[1], modalSelectAction.title))
//        val modalNode = utgService.saveNode(UiDto(
//            isNext = modalSelectAction.goNext,
//            x = modalSelectAction.coordinate[0], y = modalSelectAction.coordinate[1],
//            title = modalSelectAction.title,
//            kioskId = context.kioskId
//        ))
//        utgService.saveRel(menuNode.id, modalNode.id, NodeRelation.PATH_TO)
//
//        //완료를 눌러 옵션으로 이동
//        val modalCompleteAction = backAgent.determineBack(modalLlmUiList)
//        notificationService.sendActionCommand(kioskId, CoordinateDto(modalCompleteAction.coordinate[0], modalCompleteAction.coordinate[1], modalCompleteAction.title))
//        log.info("모달에서 빠져나옵니다. score: ${modalCompleteAction.score}, title: ${modalCompleteAction.title}")
//        log.info("UI 목록: \n ${modalLlmUiList}")
//
//        val backNode = utgService.saveNode(UiDto(
//            isNext = false,
//            x = modalCompleteAction.coordinate[0], y = modalCompleteAction.coordinate[1],
//            title = modalCompleteAction.title,
//            kioskId = context.kioskId
//        ))
//        utgService.saveRel(modalNode.id, backNode.id, NodeRelation.BACK_TO)
//
//        //옵션이 있으면 back에서 옵션으로, 옵션이 없다면 원래 노드로 관계
//        if (menuDto.options.size > 0) {
//            context.lastNode = modalNode
//        } else {
//            utgService.saveRel(backNode.id, menuNode.id, NodeRelation.BACK_TO)
//        }
//    }

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