package com.orderagentservice.global.model.response

import com.orderagentservice.global.model.dto.ExceptionDto
import com.orderagentservice.global.model.RootException

data class ApiResponse<T> (
    val success: Boolean,
    val data: T?,
    val error: ExceptionDto?
) {
    companion object {
        fun<T> success(data: T): ApiResponse<T> = ApiResponse(
            true, data, null
        )

        fun<T> fail(exception: RootException): ApiResponse<T> = ApiResponse(
            false, null, exception.toDto()
        )
    }
}