package com.orderagentservice.order.utg.model.dto

import com.orderagentservice.order.utg.model.entity.UiEntity

data class UiDto(
    val isNext: Boolean,
    val x: Int,
    val y: Int,
    val title: String,
    val kioskId: String
) {
    fun toEntity(): UiEntity = UiEntity(
        isNext = isNext,
        x = x,
        y = y,
        title = title,
        kioskId = kioskId
    )
}