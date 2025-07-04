package com.orderagentservice.global.controller

import com.orderagentservice.agent.exception.AgentManyRequestException
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.exception.CommandTimeoutException
import com.orderagentservice.order.exception.NoSuchKioskException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(AgentManyRequestException::class)
    fun handleAgentManyRequestException(e: AgentManyRequestException): ApiResponse<*> {
        return ApiResponse.fail<AgentManyRequestException>(e)
    }

    @ExceptionHandler(CommandTimeoutException::class)
    fun handleCommandTimeoutException(e: CommandTimeoutException): ApiResponse<*> {
        return ApiResponse.fail<CommandTimeoutException>(e)
    }

    @ExceptionHandler(NoSuchKioskException::class)
    fun handleNoSuchKioskException(e: NoSuchKioskException): ApiResponse<*> {
        return ApiResponse.fail<NoSuchKioskException>(e)
    }
}