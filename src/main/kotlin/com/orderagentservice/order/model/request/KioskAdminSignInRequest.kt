package com.orderagentservice.order.model.request

data class KioskAdminSignInRequest(
    val adminId: String,
    val password: String
)