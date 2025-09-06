package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.service.auto.AutoTaskExecutor
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentEditor @Autowired constructor(
    private val autoTaskExecutor: AutoTaskExecutor,
    private val graphService: UiGraphService
) {
    fun editPayment(context: GraphContext, nowUi: String) {
        val nowNodeId = graphService.findRoot(context.kioskId).id
        val autoContext = AutoOrderContext.toBasicContext(
            kioskId = context.kioskId, taskId = "EDIT", nodeId = nowNodeId,
            place = "EDIT"
        )

        //고친 곳까지 이동
        val actionList = graphService.findPath(context.kioskId, nowNodeId, nowUi)
        val last = actionList.last()
        for (action in actionList) {
            autoTaskExecutor.clickPlace(autoContext)
        }

        //complete까지 이동
        val endList = graphService.findPath(context.kioskId, last.id, SpecialNodeType.COMPLETE.title)
        for (action in endList) {
            autoTaskExecutor.clickPlace(autoContext)
        }
    }
}