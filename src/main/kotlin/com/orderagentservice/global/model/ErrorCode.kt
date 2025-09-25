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
    GLOBAL_SESSION_LOG_ERROR(2, "올바르지 않은 세션 ID입니다", HttpStatus.BAD_REQUEST),

    ORDER_NO_SUCH_KIOSK(201,"존재하지 않는 키오스크에게 sse를 보냈습니다.", HttpStatus.BAD_REQUEST),
    ORDER_COMMAND_TIMEOUT(202, "클라이언트의 명령 응답시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT),
    ORDER_NO_PATH(203, "찾으려는 UTG 경로가 없습니다.", HttpStatus.BAD_REQUEST),
    ORDER_NO_NODE(204, "찾으려는 UTG 노드가 없습니다.", HttpStatus.BAD_REQUEST),
    ORDER_ADMIN_LOGIN_FAIL(205, "키오스크 어드민 인증에 실패하였습니다.", HttpStatus.UNAUTHORIZED),
    ORDER_MENU_REQUEST_FAIL(206, "키오크스의 메뉴 정보를 얻어오지 못했습니다.", HttpStatus.BAD_REQUEST),
    ORDER_SCORE_TOO_LOW(207, "agent의 응답 점수가 너무 낮습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_UI_EXTRACT_FAIL(208, "UI Extractor Service로 요청중 에러가 발생하였습니다.", HttpStatus.BAD_REQUEST),
    ORDER_LLM_RESPONSE_PARSE_FAIL(209, "llm의 응답이 json형식이 아니여서 파싱 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_INFINITE_LOOP(210, "모달을 제대로 빠져나가지 못했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_LLM_SERVER_OVERLOAD(211, "LLM 서버의 과부하로 인해 현재 요청이 불가능합니다.", HttpStatus.SERVICE_UNAVAILABLE),
    ORDER_WORD_COMPARE_FAIL(212, "유사도 검사 요청중 오류가 발생했습니다", HttpStatus.BAD_REQUEST),
    ORDER_IMAGE_COMPARE_FAIL(213, "이미지 비교 요청중 오류가 발생했습니다", HttpStatus.BAD_REQUEST),
}