package com.orderagentservice.order.model.response

data class AutoOrderResponse(
    val taskId: String,
    val menuCount: Int,
    val payment: String,
)