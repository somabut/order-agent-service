package com.orderagentservice.order.auto.model.request

data class RandomTaskRequest(
    val kioskId: String,
    val count: Int
)