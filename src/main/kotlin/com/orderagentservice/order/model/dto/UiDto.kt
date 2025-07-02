package com.orderagentservice.order.model.dto

import com.orderagentservice.order.model.entity.UiEntity
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship

data class UiDto(
    val isNext: Boolean,
    val x: Int,
    val y: Int,
    val title: String,
    val url: String
) {
    fun toEntity(): UiEntity = UiEntity(
        isNext = isNext,
        x = x,
        y = y,
        title = title,
        url = url
    )
}