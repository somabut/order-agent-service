package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.logger
import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.service.auto.AutoTaskExecutor
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentEditor @Autowired constructor(
    private val autoTaskExecutor: AutoTaskExecutor,
    private val paymentNavigator: PaymentNavigator,
    private val graphService: UiGraphService
) {
    private val log = logger()

    fun editPayment(context: UtgContext, nowUi: String) {
        //root와 station 가져오기
        val nowNodeId = graphService.findRoot(context.kioskId).id
        context.stationNodeId = graphService.findStation(context.kioskId).id
        val autoContext = AutoOrderContext.toBasicContext(
            kioskId = context.kioskId, taskId = "EDIT", nodeId = nowNodeId,
            place = "매장"
        )

        //메뉴 가기전에 포장/매장 클릭해야할 수도 있음
        autoTaskExecutor.clickPlace(autoContext)

        //고친 곳까지 이동
        val actionList = graphService.findPath(context.kioskId, nowNodeId, nowUi)
        val last = actionList.last()
        for (action in actionList) {
            autoTaskExecutor.clickPayment(autoContext, action)
        }
        context.lastNodeId = last.id
        log.info("수정된 결제 노드로 이동합니다 -> ${last.title}")

        //complete까지 이동
        paymentNavigator.processPayment(context)
    }
}