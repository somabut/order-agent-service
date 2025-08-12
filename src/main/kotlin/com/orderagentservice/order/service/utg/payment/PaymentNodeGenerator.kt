package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.service.graph.GraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentNodeGenerator @Autowired constructor(
    private val graphService: GraphService
) {
    private val log = logger()

    fun createPaymentNode(action: AgentActionDto, context: GraphContext) {
        log.info("결제 노드를 생성합니다. go_next: ${action.goNext}, score: ${action.score}, coordinate: ${action.coordinate}, title: ${action.title}")

        val entity = graphService.saveNode(
            UiDto(
                isNext = action.goNext,
                x = action.coordinate[0], y = action.coordinate[1],
                title = action.title,
                kioskId = context.kioskId
            )
        )
        graphService.saveRel(context.lastNodeId!!, entity.id, NodeRelation.PATH_TO)

        context.lastNodeId = entity.id
    }

    fun createCompleteNode(context: GraphContext) {
        log.info("완료 노드를 생성합니다.")

        val completeEntity =  graphService.saveNode(
            UiDto(
                isNext = false,
                x = -1, y = -1,
                title = "complete",
                kioskId = context.kioskId
            )
        )
        graphService.saveRel(context.lastNodeId!!, completeEntity.id, NodeRelation.PATH_TO)
    }
}