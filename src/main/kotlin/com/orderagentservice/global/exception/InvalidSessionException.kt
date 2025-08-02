package com.orderagentservice.global.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class InvalidSessionException(
    override val errorCode: ErrorCode = ErrorCode.GLOBAL_SESSION_LOG_ERROR
) : OrderAgentException()