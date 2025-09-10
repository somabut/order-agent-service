package com.orderagentservice.order.model.dto

import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.model.type.NodeType

data class UiDto(
    val isNext: Boolean,
    val x: Int,
    val y: Int,
    val title: String,
    val kioskId: String,
    val type: NodeType,
    val modified: Boolean = true
) {
    fun toEntity(): UiEntity = UiEntity(
        isNext = isNext,
        x = x,
        y = y,
        title = title,
        kioskId = kioskId,
        type = type.name,
        modified = modified
    )
}