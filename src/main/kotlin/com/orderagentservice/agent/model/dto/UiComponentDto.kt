package com.orderagentservice.agent.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UiComponentDto(
    val x: Int,
    val y: Int,

    @JsonProperty("min_x")
    val minX: Int,

    @JsonProperty("min_y")
    val minY: Int,

    @JsonProperty("max_x")
    val maxX: Int,

    @JsonProperty("max_y")
    val maxY: Int,

    val title: String
) {
    override fun toString(): String = "{\"coordinate\": [$x, $y], \"title\": \"$title\", \"bbox\": [$minX, $minY, $maxX, $maxY]}"

    fun toAgentDto() = AgentUiDto(
        x = x, y = y,
        title = title
    )
}
