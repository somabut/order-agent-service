package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.UiDetectorManager
import org.springframework.stereotype.Component

@Component
class PaymentActionExecutorImpl(
    private val uiDetectorManager: UiDetectorManager,
    private val paymentAgent: PaymentAgent,
    private val paymentNodeIntegrator: PaymentNodeIntegrator,
    private val notificationService: NotificationService
) : PaymentActionExecutor {
    override fun selectPayment(context: GraphContext, uiList: List<UiComponentDto>): Boolean {
        val action = paymentAgent.determineAction(uiList)
        if (action.goNext == false) return false

        //노드 저장
        paymentNodeIntegrator.integratePaymentNode(action, context)

        //클릭 액션 요청
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(x = action.coordinate[0], y = action.coordinate[1], title = action.title))

        return true
    }
}