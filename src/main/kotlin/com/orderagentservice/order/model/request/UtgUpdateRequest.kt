package com.orderagentservice.order.model.request

data class CategoryUtgUpdateRequest(
    val initPayment: Boolean
)

data class MenuUtgUpdateRequest(
    val initPayment: Boolean
)

data class PaymentUtgUpdateRequest(
    val updatedTitle: String
)