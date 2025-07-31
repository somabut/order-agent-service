package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class CommandTimeoutException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_COMMAND_TIMEOUT
) : OrderAgentException(errorCode)