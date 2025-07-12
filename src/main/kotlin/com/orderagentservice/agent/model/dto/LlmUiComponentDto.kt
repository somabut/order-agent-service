package com.orderagentservice.agent.model.dto

data class LlmUiComponentDto(
    val x: Int,
    val y: Int,
    val title: String
) {
    override fun toString(): String = "{\"coordinate\": [$x, $y], \"title\": \"$title\"}"
}
