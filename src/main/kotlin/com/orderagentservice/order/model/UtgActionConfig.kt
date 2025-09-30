package com.orderagentservice.order.model

import com.orderagentservice.order.service.utg.menu.MenuActionExecutor
import com.orderagentservice.order.service.utg.payment.PaymentActionExecutor

data class UtgActionConfig(
    val menuActionExecutor: MenuActionExecutor,
    val paymentActionExecutor: PaymentActionExecutor,
)