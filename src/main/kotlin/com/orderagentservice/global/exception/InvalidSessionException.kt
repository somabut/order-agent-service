package com.orderagentservice.global.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class InvalidSessionException(
    override val errorCode: ErrorCode = ErrorCode.GLOBAL_SESSION_LOG_ERROR
) : RootException()