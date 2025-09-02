package com.orderagentservice.order.model.dto

import com.orderagentservice.order.model.entity.ScreenEntity
import com.orderagentservice.order.model.entity.UiEntity

data class ScreenDto (
    val imageUrl: String,
) {
    fun toEntity() = ScreenEntity(
        imageUrl = imageUrl,
    )
}