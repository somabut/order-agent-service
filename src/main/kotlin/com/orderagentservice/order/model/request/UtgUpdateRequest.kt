package com.orderagentservice.order.model.request

import com.orderagentservice.order.model.dto.MenuInfoDto

data class CategoryUtgUpdateRequest(
    val completeMenus: List<String>,
    val updatedCategories: List<String>,
    val initPayment: Boolean
)

data class MenuUtgUpdateRequest(
    val completeMenus: List<String>,
    val updatedMenus: List<String>,
    val initPayment: Boolean
)

data class PaymentUtgUpdateRequest(
    val updatedTitle: String
)