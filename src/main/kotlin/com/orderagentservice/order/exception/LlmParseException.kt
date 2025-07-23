package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class LlmParseException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_LLM_RESPONSE_PARSE_FAIL
) : RootException(errorCode)