package com.orderagentservice.order.model

data class AutoOrderContext(
    val kioskId: String,
    val taskId: String,
    var nodeId: String,
    val place: String?,
    var isPlaceSelected: Boolean
)