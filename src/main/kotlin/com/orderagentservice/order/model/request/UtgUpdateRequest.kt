package com.orderagentservice.order.model.request

import com.orderagentservice.order.model.dto.MenuInfoDto

data class CategoryUtgUpdateRequest(
    val initPayment: Boolean
)

data class MenuUtgUpdateRequest(
    val initPayment: Boolean
)

data class PaymentUtgUpdateRequest(
    val updatedTitle: String
)