package com.orderagentservice.order.model.response

data class KioskAdminSingInResponse(
    val adminId: String,
    val name: String,
    val email: String,
    val verified: Boolean,
    val accessToken: String,
    val tokenType: String,
    val expireIn: Long
)