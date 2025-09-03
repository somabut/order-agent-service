package com.orderagentservice.order.model.dto

import com.orderagentservice.order.model.entity.YoloEntity
import org.springframework.data.neo4j.core.schema.Property

data class YoloDto(
    val kioskId: String,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
) {
    fun toEntity() = YoloEntity(
        kioskId = kioskId,
        minX = minX,
        minY = minY,
        maxX = maxX,
        maxY = maxY
    )
}