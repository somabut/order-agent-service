package com.orderagentservice.order.model.dto

data class MenuInfoDto(
    val title: String,
    val options: List<String>,
    val category: String
) {
    override fun toString(): String = "{\"title\": \"$title\", \"option\": $options, \"category\": \"$category\"}"
}