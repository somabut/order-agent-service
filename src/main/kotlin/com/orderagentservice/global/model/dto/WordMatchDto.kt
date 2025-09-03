package com.orderagentservice.global.model.dto

import com.orderagentservice.order.model.dto.CoordinateDto

data class WordMatchDto(
    val x: Int,
    val y: Int,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
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