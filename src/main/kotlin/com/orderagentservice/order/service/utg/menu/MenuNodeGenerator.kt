package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.graph.som.SomGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.ScreenNodeGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuNodeGenerator @Autowired constructor(
    private val uiGraphService: UiGraphService,
    private val logService: LogService,
    private val somGraphService: SomGraphService,
    private val screenNodeGenerator: ScreenNodeGenerator,
) {
    fun createCategoryNode(matchDto: WordMatchDto, title: String, context: GraphContext): String {
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
                kioskId = context.kioskId
            )
        )

        uiGraphService.saveRel(context.stationNodeId!!, node.id, NodeRelationType.PATH_TO)
        uiGraphService.saveRel(node.id, context.stationNodeId!!, NodeRelationType.PATH_TO)

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeGenerator.linkNode(
            kioskId = context.kioskId,
            nodeId = node.id, screenNodeId = context.screenNodeId,
            UiComponentParams(
                minX = matchDto.minX, minY = matchDto.minY,
                maxX = matchDto.maxX, maxY = matchDto.maxY,
                title = matchDto.title
            )
        )

        context.currentCategory = node.title
        context.lastNodeId = node.id

        return node.id
    }

    fun createMenuNode(matchDto: WordMatchDto, title: String, context: GraphContext): String {
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
                kioskId = context.kioskId
            )
        )
        uiGraphService.saveRel(context.lastNodeId!!, node.id, NodeRelationType.HAS_TO)

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeGenerator.linkNode(
            kioskId = context.kioskId,
            nodeId = node.id, screenNodeId = context.screenNodeId,
            UiComponentParams(
                minX = matchDto.minX, minY = matchDto.minY,
                maxX = matchDto.maxX, maxY = matchDto.maxY,
                title = matchDto.title
            )
        )

        return node.id
    }

    fun createOptionNode(
        matchDto: WordMatchDto,
        title: String,
        menuNodeId: String,
        context: GraphContext
    ) {
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
                kioskId = context.kioskId
            )
        )
        uiGraphService.saveRel(menuNodeId, node.id, NodeRelationType.OPT_TO)

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeGenerator.linkNode(
            kioskId = context.kioskId,
            nodeId = node.id, screenNodeId = context.screenNodeId,
            UiComponentParams(
                minX = matchDto.minX, minY = matchDto.minY,
                maxX = matchDto.maxX, maxY = matchDto.maxY,
                title = matchDto.title
            )
        )
    }

    fun createBackNode(
        action: AgentBackDto,
        menuNodeId: String,
        context: GraphContext
    ): String {
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
                kioskId = context.kioskId
            )
        )
        uiGraphService.saveRel(menuNodeId, node.id, NodeRelationType.BACK_TO)

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeGenerator.linkNode(
            kioskId = context.kioskId,
            nodeId = node.id, screenNodeId = context.screenNodeId,
            UiComponentParams(
                minX = minX, minY = minY,
                maxX = maxX, maxY = maxY,
                title = action.title
            )
        )

        return node.id
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

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeGenerator.linkNode(
            kioskId = context.kioskId,
            nodeId = node.id, screenNodeId = context.screenNodeId,
            UiComponentParams(
                minX = matchDto.minX, minY = matchDto.minY,
                maxX = matchDto.maxX, maxY = matchDto.maxY,
                title = matchDto.title
            )
        )
        return node.id
    }
}