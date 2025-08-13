package com.orderagentservice.global.controller

import com.orderagentservice.agent.exception.AgentManyRequestException
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

    @ExceptionHandler(AgentManyRequestException::class)
    fun handleAgentManyRequestException(e: AgentManyRequestException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<AgentManyRequestException>(e)
    }

    @ExceptionHandler(CommandTimeoutException::class)
    fun handleCommandTimeoutException(e: CommandTimeoutException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<CommandTimeoutException>(e)
    }

    @ExceptionHandler(NoSuchKioskException::class)
    fun handleNoSuchKioskException(e: NoSuchKioskException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<NoSuchKioskException>(e)
    }

    @ExceptionHandler(PathNotFoundException::class)
    fun handlePathNotFoundException(e: PathNotFoundException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<PathNotFoundException>(e)
    }

    @ExceptionHandler(NodeNotFoundException::class)
    fun handlePathNotFoundException(e: NodeNotFoundException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<NodeNotFoundException>(e)
    }

    @ExceptionHandler(S3NotSupportedType::class)
    fun handleS3NotSupportedType(e: S3NotSupportedType): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<S3NotSupportedType>(e)
    }

    @ExceptionHandler(KioskAdminSignInException::class)
    fun handleKioskAdminSignInException(e: KioskAdminSignInException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<KioskAdminSignInException>(e)
    }

    @ExceptionHandler(MenuInfoRequestException::class)
    fun handleMenuInfoRequestException(e: MenuInfoRequestException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<MenuInfoRequestException>(e)
    }

    @ExceptionHandler(LowScoreException::class)
    fun handleLowScoreException(e: LowScoreException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<LowScoreException>(e)
    }

    @ExceptionHandler(UiExtractException::class)
    fun handleUiExtractException(e: UiExtractException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<UiExtractException>(e)
    }

    @ExceptionHandler(LlmParseException::class)
    fun handleLlmParseException(e: LlmParseException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<LlmParseException>(e)
    }

    @ExceptionHandler(InvalidSessionException::class)
    fun handleInvalidSessionException(e: InvalidSessionException): ApiResponse<*> {
        log.info("${e.errorCode.name}: ${e.message}")
        return ApiResponse.fail<LlmParseException>(e)
    }
}