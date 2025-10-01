package com.orderagentservice.order.model

import com.orderagentservice.order.service.utg.strategy.BackSelectStrategy
import com.orderagentservice.order.service.utg.strategy.CategorySelectStrategy
import com.orderagentservice.order.service.utg.strategy.MenuSelectStrategy
import com.orderagentservice.order.service.utg.strategy.OptionSelectStrategy
import com.orderagentservice.order.service.utg.strategy.PaymentSelectStrategy
import com.orderagentservice.order.service.utg.strategy.StartSelectStrategy

data class UtgActionProfile (
    val startSelectStrategy: StartSelectStrategy,
    val categorySelectStrategy: CategorySelectStrategy,
    val menuSelectStrategy: MenuSelectStrategy,
    val optionSelectStrategy: OptionSelectStrategy,
    val backSelectStrategy: BackSelectStrategy,
    val paymentSelectStrategy: PaymentSelectStrategy,
)