package com.orderagentservice.order.model.dto

data class OrderResultDto(
    val title: String,
    val category: String,
    val options: List<String>,
    val quantity: Int,
)