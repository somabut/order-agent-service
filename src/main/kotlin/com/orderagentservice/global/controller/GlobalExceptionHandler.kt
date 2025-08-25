package com.orderagentservice.global.controller

import com.orderagentservice.agent.exception.AgentManyRequestException
import com.orderagentservice.agent.exception.LlmServerOverLoadException
import com.orderagentservice.global.exception.InvalidSessionException
import com.orderagentservice.global.exception.S3NotSupportedType
import com.orderagentservice.global.model.OrderAgentException
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.logger
import com.orderagentservice.order.exception.*
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = logger()

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ApiResponse<*> {
        log.info("알 수 없는 에러 발생. ${e.message}")
        for (ele in e.stackTrace) {
            println(ele)
        }
        val exception = OrderAgentException()
        return ApiResponse.fail<OrderAgentException>(exception)
    }

    @ExceptionHandler(OrderAgentException::class)
    fun handleOrderAgentException(e: OrderAgentException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<OrderAgentException>(e)
    }
}