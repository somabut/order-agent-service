package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentNodeGenerator @Autowired constructor(
    private val graphService: UiGraphService,
    private val logService: LogService
) {
    fun createPaymentNode(action: AgentActionDto, context: GraphContext) {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId, nodeType = NodeType.PAYMENT,
                x = action.coordinate[0], y = action.coordinate[1],
                title = action.title, imageName = context.imageName
            )
        )

        val entity = graphService.saveNode(
            UiDto(
                isNext = action.goNext,
                x = action.coordinate[0], y = action.coordinate[1],
                title = action.title,
                kioskId = context.kioskId
            )
        )
        graphService.saveRel(context.lastNodeId!!, entity.id, NodeRelationType.PATH_TO)

        context.lastNodeId = entity.id
    }

    fun createCompleteNode(context: GraphContext) {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId, nodeType = NodeType.COMPLETE,
                x = -1, y = -1,
                title = SpecialNodeType.COMPLETE.title, imageName = context.imageName
            )
        )
        val completeEntity =  graphService.saveNode(
            UiDto(
                isNext = false,
                x = -1, y = -1,
                title = SpecialNodeType.COMPLETE.title,
                kioskId = context.kioskId
            )
        )
        graphService.saveRel(context.lastNodeId!!, completeEntity.id, NodeRelationType.PATH_TO)
    }
}