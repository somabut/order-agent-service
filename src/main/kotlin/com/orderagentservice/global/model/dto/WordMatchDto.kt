package com.orderagentservice.global.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.orderagentservice.order.model.dto.CoordinateDto

data class WordMatchDto(
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

    var title: String,
    val score: Double
) {
    fun toCoordinateDto(text: String) = CoordinateDto(
        x = x,
        y = y,
        title = text
    )
}