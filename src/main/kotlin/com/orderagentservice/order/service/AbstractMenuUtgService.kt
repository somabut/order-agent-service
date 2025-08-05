package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.global.service.WordSimilarityService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
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
            val menuNode = selectMenu(context, menuDto, uiList)

            //옵션 선택
            if (menuDto.options.isNotEmpty()) {
                selectOption(menuDto, menuNode, context)
            }
            selectBack(menuNode, context)
        }

        //마지막 노드를 station으로 변경
        context.lastNodeId = context.stationNodeId
    }

    private fun selectCategory(
        context: GraphContext,
        menuDto: MenuInfoDto,
        llmUiList: List<LlmUiComponentDto>
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
        context: GraphContext
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

    private fun selectBack(menuNode: UiEntity, context: GraphContext) {
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
        menuNode: UiEntity,
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
        graphService.saveRel(menuNode.id, optEntity.id, NodeRelation.OPT_TO)
    }

    private fun createBackNode(
        action: AgentBackDto,
        menuNode: UiEntity,
        context: GraphContext
    ) {
        val backEntity = graphService.saveNode(
            UiDto(
            isNext = false,
            x = action.coordinate[0], y = action.coordinate[1],
            title = action.title,
            kioskId = context.kioskId
        ))
        graphService.saveRel(menuNode.id, backEntity.id, NodeRelation.BACK_TO)
        graphService.saveRel(backEntity.id, context.lastNodeId!!, NodeRelation.BACK_TO)
    }
}