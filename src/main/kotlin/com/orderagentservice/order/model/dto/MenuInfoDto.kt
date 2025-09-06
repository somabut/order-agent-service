package com.orderagentservice.order.model.dto

import com.orderagentservice.order.model.request.AutoOrderMenu

data class MenuInfoDto(
    val title: String,
    val options: List<String>,
    val category: String
) {
    override fun toString(): String = "{\"title\": \"$title\", \"option\": $options, \"category\": \"$category\"}"

    fun toAutoOrderMenu() = AutoOrderMenu(
        category = category,
        title = title,
        count = 1,
        autoOrderOptions = emptyList()
    )
}