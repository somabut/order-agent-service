package com.orderagentservice.order.model.dto

import com.orderagentservice.order.model.entity.OcrEntity
import org.springframework.data.neo4j.core.schema.Property

data class OcrDto (
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
    val content: String,
) {
    fun toEntity() = OcrEntity(
        minX = minX,
        minY = minY,
        maxX = maxX,
        maxY = maxY,
        content = content
    )
}