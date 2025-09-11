package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentNodeIntegrator @Autowired constructor(
    private val graphService: UiGraphService,
    private val logService: LogService,
    private val screenNodeIntegrator: ScreenNodeIntegrator,
) {
    fun integratePaymentNode(action: AgentActionDto, context: GraphContext) {
        val (x, y) = action.coordinate
        val (minX, minY, maxX, maxY) = action.bbox

        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId, nodeType = NodeType.PAYMENT,
                x = x, y = y,
                title = action.title, imageName = context.imageName
            )
        )

        val node = graphService.saveNode(
            UiDto(
                isNext = action.goNext,
                x = x, y = y,
                title = action.title,
                kioskId = context.kioskId,
                type = NodeType.PAYMENT
            )
        )
        graphService.saveRel(context.lastNodeId!!, node.id, NodeRelationType.PATH_TO)

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeIntegrator.linkNode(
            kioskId = context.kioskId,
            nodeId = node.id, screenNodeId = context.screenNodeId,
            UiComponentParams(
                minX = minX, minY = minY,
                maxX = maxX, maxY = maxY,
                title = action.title
            )
        )

        context.lastNodeId = node.id
    }

    fun integrateCompleteNode(context: GraphContext) {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId, nodeType = NodeType.COMPLETE,
                x = -1, y = -1,
                title = SpecialNodeType.COMPLETE.title, imageName = context.imageName
            )
        )
        val completeEntity = graphService.saveNode(
            UiDto(
                isNext = false,
                x = -1, y = -1,
                title = SpecialNodeType.COMPLETE.title,
                kioskId = context.kioskId,
                type = NodeType.COMPLETE
            )
        )
        graphService.saveRel(context.lastNodeId!!, completeEntity.id, NodeRelationType.PATH_TO)
    }
}