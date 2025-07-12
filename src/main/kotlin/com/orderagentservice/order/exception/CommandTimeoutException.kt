package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class CommandTimeoutException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_COMMAND_TIMEOUT
) : RootException(errorCode)