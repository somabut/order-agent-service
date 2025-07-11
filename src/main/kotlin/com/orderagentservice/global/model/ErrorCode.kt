package com.orderagentservice.global.model

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: Int,
    val message: String,
    val httpStatus: HttpStatus,
) {
    UNDEFINED_ERROR(-1, "Undefined error", HttpStatus.INTERNAL_SERVER_ERROR),

    AGENT_MANY_REQUEST(101, "llm 모델에게 너무 많은 요청을 했습니다.", HttpStatus.TOO_MANY_REQUESTS),

    GLOBAL_S3_TYPE_ERROR(1, "지원하지 않는 이미지 타입입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    ORDER_NO_SUCH_KIOSK(201,"존재하지 않는 키오스크에게 sse를 보냈습니다.", HttpStatus.BAD_REQUEST),
    ORDER_COMMAND_TIMEOUT(202, "클라이언트의 명령 응답시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT),
    ORDER_NO_PATH(203, "찾으려는 UTG 경로가 없습니다.", HttpStatus.BAD_REQUEST),
    ORDER_NO_NODE(203, "찾으려는 UTG 노드가 없습니다.", HttpStatus.BAD_REQUEST),
}