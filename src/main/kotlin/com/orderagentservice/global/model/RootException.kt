package com.orderagentservice.global.model

import com.orderagentservice.global.model.dto.ExceptionDto
import java.lang.RuntimeException

open class RootException(
    open val errorCode: ErrorCode = ErrorCode.UNDEFINED_ERROR,
) : RuntimeException() {
    fun toDto() = ExceptionDto(
        errorCode = errorCode.code,
        message = errorCode.message,
    )
}