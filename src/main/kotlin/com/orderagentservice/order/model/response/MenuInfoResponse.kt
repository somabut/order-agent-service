package com.orderagentservice.order.model.response

data class MenuInfoResponse(
    val kioskId: String,
    val categories: List<Category>,
    val categoryCount: Int,
    val menuCount: Int,
    val optionCount: Int,
)

data class Category(
    val categoryId: String,
    val name: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    val menus: List<Menu>
)

data class Menu(
    val menuId: String,
    val name: String,
    val description: String,
    val price: Int,
    val saleActiveState: Boolean,
    val saleFailReason: String?,
    val createdAt: String,
    val updatedAt: String,
    val options: List<Option>
)

data class Option(
    val optionId: String,
    val name: String,
    val description: String,
    val price: Int,
    val saleActiveState: Boolean,
    val saleFailReason: String?,
    val createdAt: String,
    val updatedAt: String,
)


