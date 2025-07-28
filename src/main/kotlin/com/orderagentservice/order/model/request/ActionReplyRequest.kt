package com.orderagentservice.order.model.request

data class ActionReplyRequest(
    val x: Int,
    val y: Int,
    val cartData: CartData,
    val changes: Changes
)

data class CartData(
    val kioskType: String,
    val level: Int,
    val currentPage: String,
    val cartItems: List<Map<String, Any>>,
    val totalPrice: Int
)

data class Changes(
    val type: String,
    val description: String,
    val details: List<Map<String, Any>>
)