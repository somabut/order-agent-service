package com.orderagentservice.order.model.dto

data class AutoOrderBenchMarkDto(
    val correct: Int,
    val wrong: Int,
    val processingTime: Long
)