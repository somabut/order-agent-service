package com.orderagentservice.agent.model.dto

data class UiComponentDto(
    val x: Int,
    val y: Int,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
    val title: String
) {
    override fun toString(): String = "{\"coordinate\": [$x, $y], \"title\": \"$title\", \"bbox\": [$minX, $minY, $maxX, $maxY]}"

    fun toAgentDto() = AgentUiDto(
        x = x, y = y,
        title = title
    )
}
