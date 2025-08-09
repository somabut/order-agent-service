package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.service.WordSimilarityService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.GraphService
import com.orderagentservice.order.util.UiExtractorManager

abstract class AbstractMenuUtgService (
    private val backAgent: BackAgent,
    private val wordSimilarityService: WordSimilarityService,
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val graphService: GraphService
) {
    private val log = logger()

    protected fun navigateMenus(context: GraphContext, menuList: List<MenuInfoDto>) {
        var uiList = uiExtractorManager.getUiComponents(context.kioskId)
        for (menuDto in menuList) {
            if (menuDto.category != context.currentCategory) {
                //카테고리가 다르다면 해당 카테고리로 이동
                selectCategory(context, menuDto, uiList)
                uiList = uiExtractorManager.getUiComponents(context.kioskId)
            }

            log.info("진행 중인 메뉴: ${menuDto.title}, 카테고리: ${menuDto.category}")
            val menuNodeId = selectMenu(context, menuDto, uiList)

            //모달 처리
            handleModal(
                context = context,
                menuDto = menuDto,
                menuNodeId = menuNodeId,
                menuPageList = uiList
            )
        }

        //마지막 노드를 station으로 변경
        context.lastNodeId = context.stationNodeId
    }

    private fun selectCategory(
        context: GraphContext,
        menuDto: MenuInfoDto,
        llmUiList: List<UiComponentDto>
    ) {
        val coordinate = wordSimilarityService.findBestMatch(menuDto.category, llmUiList)
            .toCoordinateDto(menuDto.category)

        //노드 생성
        val node = createCategoryNode(coordinate, context)
        context.lastNodeId = node.id
        context.currentCategory = node.title

        //현재 카테고리 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(coordinate.x, coordinate.y, coordinate.title))
    }

    private fun selectMenu(
        context: GraphContext,
        menuDto: MenuInfoDto,
        llmUiList: List<UiComponentDto>
    ): String {
        val coordinate = wordSimilarityService.findBestMatch(menuDto.title, llmUiList)
            .toCoordinateDto(menuDto.title)

        //노드 생성
        val node = createMenuNode(coordinate, context)

        //현재 메뉴 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(coordinate.x, coordinate.y, coordinate.title))

        return node.id
    }

    private fun selectOption(
        menuDto: MenuInfoDto,
        menuNodeId: String,
        context: GraphContext
    ) {
        //메뉴의 옵션 노드 추가
        val kioskId = context.kioskId
        val llmOptList = uiExtractorManager.getUiComponents(kioskId)

        for (opt in menuDto.options) {
            val coordinate = wordSimilarityService.findBestMatch(opt, llmOptList)
                .toCoordinateDto(opt)

            //노드 생성
            createOptionNode(coordinate, menuNodeId, context)
        }
    }

    private fun selectBack(menuNodeId: String, context: GraphContext, llmOptList: List<UiComponentDto>): String {
        val kioskId = context.kioskId

        //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
        val backAction = backAgent.determineBack(llmOptList)

        //노드 생성
        val backNodeId = createBackNode(backAction, menuNodeId, context)

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        log.info("돌아가는 좌표를 클릭중입니다. 좌표: ${backAction.coordinate}")
        notificationService.sendActionCommand(kioskId, CoordinateDto(backAction.coordinate[0], backAction.coordinate[1], backAction.title))

        return backNodeId
    }

    private fun createCategoryNode(coordinate: CoordinateDto, context: GraphContext): UiEntity {
        log.info("카테고리 노드를 생성합니다. go_next: true, coordinate: [${coordinate.x}, ${coordinate.y}], title: ${coordinate.title}")

        val node = graphService.saveNode(
            UiDto(
            isNext = true,
            x = coordinate.x, y = coordinate.y,
            title = coordinate.title,
            kioskId = context.kioskId
        )
        )

        graphService.saveRel(context.stationNodeId!!, node.id, NodeRelation.PATH_TO)
        graphService.saveRel(node.id, context.stationNodeId!!, NodeRelation.PATH_TO)

        return node
    }

    private fun createMenuNode(coordinate: CoordinateDto, context: GraphContext): UiEntity {
        log.info("메뉴 노드를 생성합니다. go_next: false, coordinate: [${coordinate.x}, ${coordinate.y}], title: ${coordinate.title}")
        val node = graphService.saveNode(
            UiDto(
            isNext = false,
            x = coordinate.x, y = coordinate.y,
            title = coordinate.title,
            kioskId = context.kioskId
        ))
        graphService.saveRel(context.lastNodeId!!, node.id, NodeRelation.HAS_TO)

        return node
    }

    private fun createOptionNode(
        coordinate: CoordinateDto,
        menuNodeId: String,
        context: GraphContext
    ) {
        log.info("옵션 노드를 생성합니다. go_next: false, coordinate: [${coordinate.x}, ${coordinate.y}], title: ${coordinate.title}")
        val optEntity = graphService.saveNode(
            UiDto(
            isNext = false,
            x = coordinate.x, y = coordinate.y,
            title = coordinate.title,
            kioskId = context.kioskId
        ))
        graphService.saveRel(menuNodeId, optEntity.id, NodeRelation.OPT_TO)
    }

    private fun createBackNode(
        action: AgentBackDto,
        menuNodeId: String,
        context: GraphContext
    ): String {
        val backEntity = graphService.saveNode(
            UiDto(
            isNext = false,
            x = action.coordinate[0], y = action.coordinate[1],
            title = action.title,
            kioskId = context.kioskId
        ))
        graphService.saveRel(menuNodeId, backEntity.id, NodeRelation.BACK_TO)

        return backEntity.id
    }

    private fun createModalNode(
        context: GraphContext,
        matchDto: WordMatchDto,
        menuDto: MenuInfoDto,
        menuNodeId: String
    ): String {
        graphService.changeTitle(menuNodeId, context.kioskId, "modal:${menuDto.title}")
        val node = graphService.saveNode(
            UiDto(
                isNext = true, kioskId = context.kioskId,
                x = matchDto.x, y = matchDto.y,
                title = menuDto.title
            )
        )
        graphService.saveRel(menuNodeId, node.id, NodeRelation.HAS_TO)

        return node.id
    }

    private fun handleModal(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        menuPageList: List<UiComponentDto>
    ) {
        //현재 메뉴를 일단 클릭한 상황
        var nodeId = menuNodeId
        var uiList = uiExtractorManager.getUiComponents(context.kioskId)

        if (menuDto.options.isEmpty()) {
            //옵션이 없는 경우
            if (checkMenuPage(menuPageList, uiList) == false) {
                nodeId = selectModal(
                    context = context,
                    menuDto = menuDto,
                    menuNodeId = menuNodeId,
                    uiList = uiList
                )
            }
            graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelation.BACK_TO)
        } else {
            //옵션이 있는 경우
            if (checkOptionPage(menuDto.options, uiList) == false) {
                nodeId = selectModal(
                    context = context,
                    menuDto = menuDto,
                    menuNodeId = menuNodeId,
                    uiList = uiList
                )
            }

            //옵션으로 왔으므로 옵션선택
            selectOption(menuDto, nodeId, context)

            //옵션을 선택하고 원래 페이지도 이동
            uiList = uiExtractorManager.getUiComponents(context.kioskId)
            while (checkMenuPage(menuPageList, uiList) == false) {
                nodeId = selectBack(nodeId, context, uiList)
                uiList = uiExtractorManager.getUiComponents(context.kioskId)
            }
            graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelation.BACK_TO)
        }
    }

    private fun selectModal(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): String {
        //메뉴를 다시 선택해야 할 수도 있으므로 클릭
        var nodeId = menuNodeId
        val matchDto = wordSimilarityService.findBestMatch(menuDto.title, uiList)
        if (matchDto.score >= 0.6) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(x = matchDto.x, y = matchDto.y, title = matchDto.title))
        }

        //노드명 변경
        nodeId = createModalNode(
            context = context,
            matchDto = matchDto,
            menuDto = menuDto,
            menuNodeId = nodeId
        )

        //다음으로 이동
        val uiList = uiExtractorManager.getUiComponents(context.kioskId)
        nodeId = selectBack(nodeId, context, uiList)

        return nodeId
    }

    private fun checkMenuPage(menuPageList: List<UiComponentDto>, uiList: List<UiComponentDto>): Boolean {
        val sourceList = menuPageList.map { it.title }

        val result = wordSimilarityService.determinePage(sourceList, uiList)
        return result
    }

    private fun checkOptionPage(optionList: List<String>, uiList: List<UiComponentDto>): Boolean {
        val result = wordSimilarityService.determinePage(optionList, uiList)
        return result
    }
}