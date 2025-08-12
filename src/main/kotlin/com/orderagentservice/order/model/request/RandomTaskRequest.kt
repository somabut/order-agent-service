package com.orderagentservice.order.model.request

data class RandomTaskRequest(
    val kioskId: String,
    val count: Int
)