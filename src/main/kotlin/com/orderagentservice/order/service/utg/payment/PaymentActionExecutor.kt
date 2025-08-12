package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.order.model.GraphContext

interface PaymentActionExecutor {
    fun selectPayment(context: GraphContext): Boolean
}