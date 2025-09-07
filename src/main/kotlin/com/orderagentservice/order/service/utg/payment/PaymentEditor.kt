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
        context.stationNodeId = graphService.findStation(context.kioskId).id
        val autoContext = AutoOrderContext.toBasicContext(
            kioskId = context.kioskId, taskId = "EDIT", nodeId = nowNodeId,
            place = "EDIT"
        )

        //메뉴 가기전에 포장/매장 클릭해야할 수도 있음
        autoTaskExecutor.clickPlace(autoContext)

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