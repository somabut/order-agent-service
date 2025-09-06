package com.orderagentservice.order.model.request

import com.orderagentservice.order.model.dto.MenuInfoDto

data class CategoryUtgUpdateRequest(
    val categoryList: List<String>,
    val pendingList: List<MenuInfoDto>,
    val initPayment: Boolean
)

data class MenuUtgUpdateRequest(
    val updatedMenus: List<MenuInfoDto>,
    val pendingMenus: List<MenuInfoDto>,
    val initPayment: Boolean
)

data class PaymentUtgUpdateRequest(
    val updatedTitle: String
)