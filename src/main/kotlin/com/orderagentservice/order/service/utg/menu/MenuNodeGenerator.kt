package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.graph.screen.ScreenGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuNodeGenerator @Autowired constructor(
    private val uiGraphService: UiGraphService,
    private val logService: LogService,
    private val screenGraphService: ScreenGraphService
) {
    fun createCategoryNode(coordinate: CoordinateDto, context: GraphContext): String {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.CATEGORY,
                x = coordinate.x, y = coordinate.y,
                title = coordinate.title, imageName = context.imageName
            )
        )

        val node = uiGraphService.saveNode(
            UiDto(
                isNext = true,
                x = coordinate.x, y = coordinate.y,
                title = coordinate.title,
                kioskId = context.kioskId
            )
        )

        uiGraphService.saveRel(context.stationNodeId!!, node.id, NodeRelationType.PATH_TO)
        uiGraphService.saveRel(node.id, context.stationNodeId!!, NodeRelationType.PATH_TO)

        screenGraphService.saveRel(node.id, context.screenNodeId)

        context.currentCategory = node.title
        context.lastNodeId = node.id
        context.history.add(node.toAgentActionDto())

        return node.id
    }

    fun createMenuNode(coordinate: CoordinateDto, context: GraphContext): String {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.MENU,
                x = coordinate.x, y = coordinate.y,
                title = coordinate.title, imageName = context.imageName
            )
        )
        val node = uiGraphService.saveNode(
            UiDto(
                isNext = false,
                x = coordinate.x, y = coordinate.y,
                title = coordinate.title,
                kioskId = context.kioskId
            )
        )
        uiGraphService.saveRel(context.lastNodeId!!, node.id, NodeRelationType.HAS_TO)

        screenGraphService.saveRel(node.id, context.screenNodeId)

        context.history.add(node.toAgentActionDto())

        return node.id
    }

    fun createOptionNode(
        coordinate: CoordinateDto,
        menuNodeId: String,
        context: GraphContext
    ) {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.OPTION,
                x = coordinate.x, y = coordinate.y,
                title = coordinate.title, imageName = context.imageName
            )
        )
        val optEntity = uiGraphService.saveNode(
            UiDto(
                isNext = false,
                x = coordinate.x, y = coordinate.y,
                title = coordinate.title,
                kioskId = context.kioskId
            )
        )
        uiGraphService.saveRel(menuNodeId, optEntity.id, NodeRelationType.OPT_TO)

        screenGraphService.saveRel(optEntity.id, context.screenNodeId)

        context.history.add(optEntity.toAgentActionDto())
    }

    fun createBackNode(
        action: AgentBackDto,
        menuNodeId: String,
        context: GraphContext
    ): String {
        val x = action.coordinate[0]
        val y = action.coordinate[1]
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.BACK,
                x = x, y = y,
                title = action.title, imageName = context.imageName
            )
        )
        val backEntity = uiGraphService.saveNode(
            UiDto(
                isNext = false,
                x = x, y = y,
                title = action.title,
                kioskId = context.kioskId
            )
        )
        uiGraphService.saveRel(menuNodeId, backEntity.id, NodeRelationType.BACK_TO)

        screenGraphService.saveRel(backEntity.id, context.screenNodeId)

        context.history.add(backEntity.toAgentActionDto())

        return backEntity.id
    }

    fun createModalNode(
        context: GraphContext,
        matchDto: WordMatchDto,
        menuDto: MenuInfoDto,
        menuNodeId: String
    ): String {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.MODAL,
                x = matchDto.x, y = matchDto.y,
                title = menuDto.title, imageName = context.imageName
            )
        )
        val node = uiGraphService.saveNode(
            UiDto(
                isNext = true, kioskId = context.kioskId,
                x = matchDto.x, y = matchDto.y,
                title = menuDto.title
            )
        )
        uiGraphService.saveRel(menuNodeId, node.id, NodeRelationType.HAS_TO)

        screenGraphService.saveRel(node.id, context.screenNodeId)

        context.history.add(node.toAgentActionDto())

        return node.id
    }
}