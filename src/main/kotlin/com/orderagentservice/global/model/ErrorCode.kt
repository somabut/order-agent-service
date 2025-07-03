package com.orderagentservice.global.model

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: Int,
    val message: String,
    val httpStatus: HttpStatus,
) {
    UNDEFINED_ERROR(-1, "Undefined error", HttpStatus.INTERNAL_SERVER_ERROR),

    AGENT_MANY_REQUEST(101, "llm 모델에게 너무 많은 요청을 했습니다.", HttpStatus.TOO_MANY_REQUESTS),

    ORDER_NO_SUCH_KIOSK(201,"존재하지 않는 키오스크에게 sse를 보냈습니다.", HttpStatus.BAD_REQUEST),
    ORDER_COMMAND_TIMEOUT(202, "클라이언트의 명령 응답시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT)
}