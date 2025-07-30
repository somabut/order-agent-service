package com.orderagentservice.order.model.request

data class ActionReplyRequest(
    val x: Int,
    val y: Int,
    val cartData: CartData?,
    val changes: Changes?
)

data class CartData(
    val kioskType: String,
    val level: Int,
    val currentPage: String,
    val cartItems: List<Item>,
    val totalPrice: Int,
    val paymentMethod: String?
)

data class Changes(
    val type: String,
    val description: String,
    val details: List<Details>
)

data class Details(
    val action: String,
    val item: Item?
)

data class Item(
    val name: String,
    val price: Int,
    val quantity: Int,
    val total: Int,
    val type: String
)