package com.orderagentservice.global.model.response

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.orderagentservice.global.model.dto.ExceptionDto
import com.orderagentservice.global.model.RootException
import com.orderagentservice.global.util.HttpStatusFlexibleDeserializer
import org.springframework.http.HttpStatus

data class ApiResponse<T> (
    val success: Boolean,
    val data: T?,
    val error: ExceptionDto?,
    @JsonDeserialize(using = HttpStatusFlexibleDeserializer::class)
    val httpStatus: HttpStatus?
) {
    companion object {
        fun<T> success(data: T): ApiResponse<T> = ApiResponse(
            true, data, null, HttpStatus.OK
        )

        fun<T> fail(exception: RootException): ApiResponse<T> = ApiResponse(
            false, null, exception.toDto(), exception.errorCode.httpStatus
        )
    }
}