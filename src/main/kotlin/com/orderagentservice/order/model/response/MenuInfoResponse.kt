package com.orderagentservice.order.model.response

data class MenuInfoResponse(
    val kioskId: String,
    val categories: List<Category>,
    val categoryCount: Int,
    val menuCount: Int,
    val optionCount: Int,
    val lastUpdated: String
)

data class Category(
    val name: String,
    val description: String,
    val menus: List<Menu>
)

data class Menu(
    val name: String,
    val description: String,
    val price: Int,
    val saleActiveState: Boolean,
    val saleFailReason: String,
    val options: List<Option>
)

data class Option(
    val name: String,
    val description: String,
    val price: Int,
    val saleActiveState: Boolean
)


