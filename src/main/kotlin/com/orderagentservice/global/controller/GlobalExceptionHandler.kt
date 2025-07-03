package com.orderagentservice.global.controller

import com.orderagentservice.agent.exception.AgentManyRequestException
import com.orderagentservice.global.model.response.ApiResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(AgentManyRequestException::class)
    fun handleAgentManyRequestException(e: AgentManyRequestException): ApiResponse<*> {
        return ApiResponse.fail<AgentManyRequestException>(e)
    }
}