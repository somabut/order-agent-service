package com.orderagentservice.global.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RatioCoordinate(
    @JsonProperty("min_x")
    val minX: Double,

    @JsonProperty("min_y")
    val minY: Double,

    @JsonProperty("max_x")
    val maxX: Double,

    @JsonProperty("max_y")
    val maxY: Double,
) {
    fun toPixel(width: Int, height: Int): PixelCoordinate = PixelCoordinate(
        minX = (minX * width).toInt(),
        minY = (minY * height).toInt(),
        maxX = (maxX * width).toInt(),
        maxY = (maxY * height).toInt(),
    )
}

data class PixelCoordinate(
    @JsonProperty("min_x")
    val minX: Int,

    @JsonProperty("min_y")
    val minY: Int,

    @JsonProperty("max_x")
    val maxX: Int,

    @JsonProperty("max_y")
    val maxY: Int,
) {
    fun getCenter(): Pair<Int, Int> = Pair((minX + maxX) / 2, (minY + maxY) / 2)
}