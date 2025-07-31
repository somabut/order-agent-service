package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class LowScoreException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_SCORE_TOO_LOW
): OrderAgentException()