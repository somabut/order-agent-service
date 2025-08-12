package com.orderagentservice.order.utg.service

import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.logger
import com.orderagentservice.order.utg.model.GraphContext
import com.orderagentservice.order.utg.NodeRelation
import com.orderagentservice.order.utg.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.utg.model.dto.UiDto
import com.orderagentservice.order.service.GraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuNodeGenerator @Autowired constructor(
    private val graphService: GraphService
) {
    private val log = logger()

    fun createCategoryNode(coordinate: CoordinateDto, context: GraphContext): String {
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

        context.currentCategory = node.title
        context.lastNodeId = node.id
        context.history.add(node.toAgentActionDto())

        return node.id
    }

    fun createMenuNode(coordinate: CoordinateDto, context: GraphContext): String {
        log.info("메뉴 노드를 생성합니다. go_next: false, coordinate: [${coordinate.x}, ${coordinate.y}], title: ${coordinate.title}")
        val node = graphService.saveNode(
            UiDto(
                isNext = false,
                x = coordinate.x, y = coordinate.y,
                title = coordinate.title,
                kioskId = context.kioskId
            )
        )
        graphService.saveRel(context.lastNodeId!!, node.id, NodeRelation.HAS_TO)

        context.history.add(node.toAgentActionDto())

        return node.id
    }

    fun createOptionNode(
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
            )
        )
        graphService.saveRel(menuNodeId, optEntity.id, NodeRelation.OPT_TO)

        context.history.add(optEntity.toAgentActionDto())
    }

    fun createBackNode(
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
            )
        )
        graphService.saveRel(menuNodeId, backEntity.id, NodeRelation.BACK_TO)

        context.history.add(backEntity.toAgentActionDto())

        return backEntity.id
    }

    fun createModalNode(
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

        context.history.add(node.toAgentActionDto())

        return node.id
    }
}