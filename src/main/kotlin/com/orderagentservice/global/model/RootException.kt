package com.orderagentservice.global.model

import com.orderagentservice.global.model.dto.ExceptionDto
import java.lang.RuntimeException

class RootException(
    open val errorCode: ErrorCode,
) : RuntimeException() {
    fun toDto() = ExceptionDto(
        errorCode = errorCode.code,
        message = errorCode.message,
    )
}