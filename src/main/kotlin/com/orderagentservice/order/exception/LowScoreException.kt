package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class LowScoreException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_SCORE_TOO_LOW
): RootException()