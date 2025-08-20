package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class UtgInfiniteLoopException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_INFINITE_LOOP
) : OrderAgentException(errorCode)