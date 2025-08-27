package com.orderagentservice.order.model.log

import com.orderagentservice.order.model.dto.MenuInfoDto

data class OrderStartLog(
    val kioskId: String,
    val menus: List<MenuInfoDto>,
)

data class OrderProcessLog(
    val kioskId: String,
    val type: String,
    val x: String,
    val y: String,
    val title: String
)

data class OrderEndLog(
    val kioskId: String,
    val menus: List<MenuInfoDto>,
    val processingTime: Long,
)