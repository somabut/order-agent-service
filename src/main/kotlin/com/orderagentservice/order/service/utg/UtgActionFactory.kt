package com.orderagentservice.order.service.utg

import com.orderagentservice.order.model.UtgActionConfig
import com.orderagentservice.order.model.type.UtgActionType
import com.orderagentservice.order.service.utg.menu.MenuActionExecutor
import com.orderagentservice.order.service.utg.payment.PaymentActionExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UtgActionFactory @Autowired constructor(
    private val menuActionExecutor: MenuActionExecutor,
    private val paymentActionExecutor: PaymentActionExecutor
) {
    fun getConfig(type: UtgActionType): UtgActionConfig {

    }
}