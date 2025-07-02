package com.orderagentservice.global.model

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: Int,
    val message: String,
    val httpStatus: HttpStatus,
) {
    UNDEFINED_ERROR(-1, "Undefined error", HttpStatus.INTERNAL_SERVER_ERROR),
}