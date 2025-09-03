package com.orderagentservice.order.model.dto

import com.orderagentservice.order.model.entity.SomEntity
import org.springframework.data.neo4j.core.schema.Property

data class SomDto (
    val kioskId: String,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
    val content: String,
) {
    fun toEntity() = SomEntity(
        kioskId = kioskId,
        minX = minX,
        minY = minY,
        maxX = maxX,
        maxY = maxY,
        content = content
    )
}