package com.orderagentservice.global.controller

import com.orderagentservice.agent.exception.AgentManyRequestException
import com.orderagentservice.global.exception.S3NotSupportedType
import com.orderagentservice.global.model.RootException
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.exception.*
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RootException): ApiResponse<*> {
        return ApiResponse.fail<RootException>(e)
    }

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

    @ExceptionHandler(PathNotFoundException::class)
    fun handlePathNotFoundException(e: PathNotFoundException): ApiResponse<*> {
        return ApiResponse.fail<PathNotFoundException>(e)
    }

    @ExceptionHandler(NodeNotFoundException::class)
    fun handlePathNotFoundException(e: NodeNotFoundException): ApiResponse<*> {
        return ApiResponse.fail<NodeNotFoundException>(e)
    }

    @ExceptionHandler(S3NotSupportedType::class)
    fun handleS3NotSupportedType(e: S3NotSupportedType): ApiResponse<*> {
        return ApiResponse.fail<S3NotSupportedType>(e)
    }

    @ExceptionHandler(KioskAdminSignInException::class)
    fun handleKioskAdminSignInException(e: KioskAdminSignInException): ApiResponse<*> {
        return ApiResponse.fail<KioskAdminSignInException>(e)
    }

    @ExceptionHandler(MenuInfoRequestException::class)
    fun handleMenuInfoRequestException(e: MenuInfoRequestException): ApiResponse<*> {
        return ApiResponse.fail<MenuInfoRequestException>(e)
    }

    @ExceptionHandler(LowScoreException::class)
    fun handleLowScoreException(e: LowScoreException): ApiResponse<*> {
        return ApiResponse.fail<LowScoreException>(e)
    }

    @ExceptionHandler(UiExtractException::class)
    fun handleUiExtractException(e: UiExtractException): ApiResponse<*> {
        return ApiResponse.fail<UiExtractException>(e)
    }
}