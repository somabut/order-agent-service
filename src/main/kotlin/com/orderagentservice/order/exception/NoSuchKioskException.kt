package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class NoSuchKioskException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_NO_SUCH_KIOSK
) : RootException(errorCode)