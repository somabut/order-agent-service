package com.orderagentservice.order.model.request

import com.orderagentservice.order.model.type.StrategyType

data class UtgStrategyRequest(
    val startStrategy: String,
    val optionStrategy: String,
    val backStrategy: String,
    val paymentStrategy: String,
)