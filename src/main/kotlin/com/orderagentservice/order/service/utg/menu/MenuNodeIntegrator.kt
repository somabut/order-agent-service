package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.result.NodeCreationResult
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuNodeIntegrator @Autowired constructor(
    private val uiGraphService: UiGraphService,
    private val logService: LogService,
) {
    fun integrateCategoryNode(matchDto: WordMatchDto, title: String, context: UtgContext): NodeCreationResult {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.CATEGORY,
                x = matchDto.x, y = matchDto.y,
                title = title, imageName = context.imageName
            )
        )

        val node = uiGraphService.saveNode(
            UiDto(
                isNext = true,
                x = matchDto.x, y = matchDto.y,
                title = title,
                kioskId = context.kioskId,
                type = NodeType.CATEGORY
            )
        )

        uiGraphService.saveRel(context.stationNodeId!!, node.id, NodeRelationType.PATH_TO)
        uiGraphService.saveRel(node.id, context.stationNodeId!!, NodeRelationType.PATH_TO)

        //현재 노드 갱신
        context.currentCategory = node.title
        context.lastNodeId = node.id

        return NodeCreationResult(
            nodeId = node.id,
            uiComponentParams = UiComponentParams(
                minX = matchDto.minX, minY = matchDto.minY,
                maxX = matchDto.maxX, maxY = matchDto.maxY,
                title = matchDto.title,
            )
        )
    }

    fun integrateMenuNode(matchDto: WordMatchDto, title: String, context: UtgContext): NodeCreationResult {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.MENU,
                x = matchDto.x, y = matchDto.y,
                title = title, imageName = context.imageName
            )
        )
        val node = uiGraphService.saveNode(
            UiDto(
                isNext = false,
                x = matchDto.x, y = matchDto.y,
                title = title,
                kioskId = context.kioskId,
                type = NodeType.MENU
            )
        )
        uiGraphService.saveRel(context.lastNodeId!!, node.id, NodeRelationType.HAS_TO)

        //메뉴 노드는 Screen과 연결할 필요 없음
        return NodeCreationResult(
            nodeId = node.id,
            uiComponentParams = UiComponentParams(
                minX = matchDto.minX, minY = matchDto.minY,
                maxX = matchDto.maxX, maxY = matchDto.maxY,
                title = matchDto.title
            )
        )
    }

    fun integrateOptionNode(
        matchDto: WordMatchDto,
        title: String,
        menuNodeId: String,
        context: UtgContext
    ): NodeCreationResult {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.OPTION,
                x = matchDto.x, y = matchDto.y,
                title = title, imageName = context.imageName
            )
        )
        val node = uiGraphService.saveNode(
            UiDto(
                isNext = false,
                x = matchDto.x, y = matchDto.y,
                title = title,
                kioskId = context.kioskId,
                type = NodeType.OPTION
            )
        )
        uiGraphService.saveRel(menuNodeId, node.id, NodeRelationType.OPT_TO)

        return NodeCreationResult(
            nodeId = node.id,
            uiComponentParams = UiComponentParams(
                minX = matchDto.minX, minY = matchDto.minY,
                maxX = matchDto.maxX, maxY = matchDto.maxY,
                title = matchDto.title
            )
        )
    }

    fun integrateBackNode(
        action: AgentBackDto,
        menuNodeId: String,
        context: UtgContext
    ): NodeCreationResult {
        val (x, y) = action.coordinate
        val (minX, minY, maxX, maxY) = action.bbox
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId,
                nodeType = NodeType.BACK,
                x = x, y = y,
                title = action.title, imageName = context.imageName
            )
        )
        val node = uiGraphService.saveNode(
            UiDto(
                isNext = false,
                x = x, y = y,
                title = action.title,
                kioskId = context.kioskId,
                type = NodeType.BACK
            )
        )
        uiGraphService.saveRel(menuNodeId, node.id, NodeRelationType.BACK_TO)

        return NodeCreationResult(
            nodeId = node.id,
            uiComponentParams = UiComponentParams(
                minX = minX, minY = minY,
                maxX = maxX, maxY = maxY,
                title = action.title
            )
        )
    }

    fun integrateModalNode(
        context: UtgContext,
        matchDto: WordMatchDto,
        menuDto: MenuInfoDto,
        menuNodeId: String
    ): NodeCreationResult {
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
                title = menuDto.title,
                type = NodeType.MODAL
            )
        )
        uiGraphService.saveRel(menuNodeId, node.id, NodeRelationType.HAS_TO)

        return NodeCreationResult(
            nodeId = node.id,
            uiComponentParams = UiComponentParams(
                minX = matchDto.minX, minY = matchDto.minY,
                maxX = matchDto.maxX, maxY = matchDto.maxY,
                title = matchDto.title
            )
        )
    }
}