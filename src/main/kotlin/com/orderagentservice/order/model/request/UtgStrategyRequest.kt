package com.orderagentservice.order.model.request

data class UtgStrategyRequest(
    val startStrategy: String,
    val optionStrategy: String,
    val backStrategy: String,
    val paymentStrategy: String,
)