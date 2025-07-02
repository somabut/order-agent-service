package com.orderagentservice.agent.model.dto

data class UiActionDto (
    val goNext: Boolean,
    val score: Float,
    val coordinate: List<Int>,
    val title: String
)