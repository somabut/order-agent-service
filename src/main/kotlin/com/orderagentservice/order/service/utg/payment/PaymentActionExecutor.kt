package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.GraphContext

interface PaymentActionExecutor {
    fun selectPayment(context: GraphContext, uiList: List<UiComponentDto>): Boolean
}