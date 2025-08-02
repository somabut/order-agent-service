package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.MissingComponentAgent
import com.orderagentservice.agent.PageAgent
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.service.WordSimilarityService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphInitializeContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
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
    private val wordSimilarityService: WordSimilarityService,
    private val placeGraphInitializeService: PlaceGraphInitializeService,
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val utgService: UtgService
) {
    private val log = logger()

    private val MAX_MODAL_TO_MENU_COUNT = 3

    @Transactional
    fun initializeGraph(context: GraphInitializeContext, menuList: List<MenuInfoDto>) {
        log.info("메뉴 utg 생성 시작")
        val startTime = System.nanoTime()

        // root, station노드 초기화
        setupNode(context)

        //포장/매장 찾기
        placeGraphInitializeService.initializeGraph(context)

        //루프를 돌며 메뉴들을 모두 탐색
        navigateMenus(context, menuList)

        //포장/매장 찾기
        if (context.determinePlace == false) {
            placeGraphInitializeService.initializeGraph(context)
        }

        val endTime = System.nanoTime()
        log.info("메뉴 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    private fun setupNode(context: GraphInitializeContext) {
        log.info("root노드와 station노드를 초기화합니다.")
        val kioskId = context.kioskId
        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "root"
        )
        context.lastNodeId = utgService.saveNode(rootUiDto).id

        val stationNode = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "station"
        )
        context.stationNodeId = utgService.saveNode(stationNode).id
        utgService.saveNode(stationNode)
    }

    private fun navigateMenus(context: GraphInitializeContext, menuList: List<MenuInfoDto>) {
        for (menuDto in menuList) {
            var uiList: List<LlmUiComponentDto> = listOf()
            if (menuDto.category != context.nowCategory) {
                //카테고리가 다르다면 해당 카테고리로 이동
                uiList = uiExtractorManager.getUiComponents(context.kioskId)
                selectCategory(context, menuDto, uiList)
            }

            log.info("진행 중인 메뉴: ${menuDto.title}, 카테고리: ${menuDto.category}")
            val node = selectMenu(context, menuDto, uiList)

            //옵션 선택
            if (menuDto.options.isNotEmpty()) {
                selectOption(menuDto, node, context)
            }
            selectBack(node, context)
        }

        //마지막 노드를 station으로 변경
        context.lastNodeId = context.stationNodeId
    }

    private fun selectCategory(
        context: GraphInitializeContext,
        menuDto: MenuInfoDto,
        llmUiList: List<LlmUiComponentDto>
    ) {
        val coordinate = wordSimilarityService.findBestMatch(menuDto.category, llmUiList)
            .toCoordinateDto(menuDto.category)

        //노드 생성
        val node = createCategoryNode(coordinate, context)
        context.lastNodeId = node.id
        context.nowCategory = node.title

        //현재 카테고리 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(coordinate.x, coordinate.y, coordinate.title))
    }

    private fun selectMenu(
        context: GraphInitializeContext,
        menuDto: MenuInfoDto,
        llmUiList: List<LlmUiComponentDto>
    ): UiEntity {
        val coordinate = wordSimilarityService.findBestMatch(menuDto.title, llmUiList)
            .toCoordinateDto(menuDto.title)

        //노드 생성
        val node = createMenuNode(coordinate, context)

        //현재 메뉴 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(coordinate.x, coordinate.y, coordinate.title))

        return node
    }

    private fun selectOption(
        menuDto: MenuInfoDto,
        menuNode: UiEntity,
        context: GraphInitializeContext
    ) {
        //메뉴의 옵션 노드 추가
        val kioskId = context.kioskId
        val llmOptList = uiExtractorManager.getUiComponents(kioskId)

        for (opt in menuDto.options) {
            val coordinate = wordSimilarityService.findBestMatch(opt, llmOptList)
                .toCoordinateDto(opt)

            //노드 생성
            createOptionNode(coordinate, menuNode, context)
        }
    }

    private fun selectBack(menuNode: UiEntity, context: GraphInitializeContext) {
        val kioskId = context.kioskId
        val llmOptList = uiExtractorManager.getUiComponents(kioskId)

        //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
        val backAction = backAgent.determineBack(llmOptList)

        //노드 생성
        createBackNode(backAction, menuNode, context)

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        log.info("돌아가는 좌표를 클릭중입니다. 좌표: ${backAction.coordinate}")
        notificationService.sendActionCommand(kioskId, CoordinateDto(backAction.coordinate[0], backAction.coordinate[1], backAction.title))
    }

    private fun createCategoryNode(coordinate: CoordinateDto, context: GraphInitializeContext): UiEntity {
        log.info("카테고리 노드를 생성합니다. go_next: true, coordinate: [${coordinate.x}, ${coordinate.y}], title: ${coordinate.title}")

        val node = utgService.saveNode(UiDto(
            isNext = true,
            x = coordinate.x, y = coordinate.y,
            title = coordinate.title,
            kioskId = context.kioskId
        ))
//        utgService.saveRel(context.lastNode!!.id, node.id, NodeRelation.PATH_TO)
//        utgService.saveRel(node.id, context.lastNode!!.id, NodeRelation.PATH_TO)

        utgService.saveRel(context.stationNodeId!!, node.id, NodeRelation.PATH_TO)
        utgService.saveRel(node.id, context.stationNodeId!!, NodeRelation.PATH_TO)

        return node
    }

    private fun createMenuNode(coordinate: CoordinateDto, context: GraphInitializeContext): UiEntity {
        log.info("메뉴 노드를 생성합니다. go_next: false, coordinate: [${coordinate.x}, ${coordinate.y}], title: ${coordinate.title}")
        val node = utgService.saveNode(UiDto(
            isNext = false,
            x = coordinate.x, y = coordinate.y,
            title = coordinate.title,
            kioskId = context.kioskId
        ))
        utgService.saveRel(context.lastNodeId!!, node.id, NodeRelation.HAS_TO)

        return node
    }

    private fun createOptionNode(
        coordinate: CoordinateDto,
        menuNode: UiEntity,
        context: GraphInitializeContext
    ) {
        log.info("옵션 노드를 생성합니다. go_next: false, coordinate: [${coordinate.x}, ${coordinate.y}], title: ${coordinate.title}")
        val optEntity = utgService.saveNode(UiDto(
            isNext = false,
            x = coordinate.x, y = coordinate.y,
            title = coordinate.title,
            kioskId = context.kioskId
        ))
        utgService.saveRel(menuNode.id, optEntity.id, NodeRelation.OPT_TO)
    }

    private fun createBackNode(
        action: AgentBackDto,
        menuNode: UiEntity,
        context: GraphInitializeContext
    ) {
        val backEntity = utgService.saveNode(UiDto(
            isNext = false,
            x = action.coordinate[0], y = action.coordinate[1],
            title = action.title,
            kioskId = context.kioskId
        ))
        utgService.saveRel(menuNode.id, backEntity.id, NodeRelation.BACK_TO)
        utgService.saveRel(backEntity.id, context.lastNodeId!!, NodeRelation.BACK_TO)
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