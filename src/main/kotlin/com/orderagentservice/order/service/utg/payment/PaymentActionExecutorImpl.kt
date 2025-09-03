package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.AgentUiDto
import com.orderagentservice.order.model.type.ExtractType
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.UiDetectorManager
import org.springframework.stereotype.Component

@Component
class PaymentActionExecutorImpl(
    private val uiDetectorManager: UiDetectorManager,
    private val paymentAgent: PaymentAgent,
    private val paymentNodeGenerator: PaymentNodeGenerator,
    private val notificationService: NotificationService
) : PaymentActionExecutor {
    override fun selectPayment(context: GraphContext): Boolean {
        val llmUiList = uiDetectorManager.getUiComponents(context).ocrElements
        val action = paymentAgent.determineAction(llmUiList)

        //노드 저장
        paymentNodeGenerator.createPaymentNode(action, context)

        //클릭 액션 요청
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(x = action.coordinate[0], y = action.coordinate[1], title = action.title))

        return action.goNext
    }
}