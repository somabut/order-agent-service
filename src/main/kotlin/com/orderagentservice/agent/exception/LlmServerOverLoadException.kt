package com.orderagentservice.agent.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class LlmServerOverLoadException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_LLM_SERVER_OVERLOAD
) : OrderAgentException(errorCode)