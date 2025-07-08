package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class PathNotFoundException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_NO_PATH
) : RootException(errorCode)