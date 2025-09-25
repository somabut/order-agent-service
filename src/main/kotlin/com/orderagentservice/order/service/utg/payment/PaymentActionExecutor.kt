package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext

interface PaymentActionExecutor {
    fun selectPayment(context: UtgContext, uiList: List<UiComponentDto>): Boolean
}