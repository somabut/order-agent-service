package com.orderagentservice.global.model.dto

import com.orderagentservice.jsonMapper

data class LogDto(
    val message: String,
    val kioskId: String,
    val taskId: String
)