package com.orderagentservice.order.model.dto

data class UiComponentDto(
    val x: Int,
    val y: Int,
    val title: String
) {
    override fun toString(): String = "{\"coordinate\": [$x, $y], \"title\": \"$title\"}"
}