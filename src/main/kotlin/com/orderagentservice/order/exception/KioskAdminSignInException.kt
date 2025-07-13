package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class KioskAdminSignInException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_ADMIN_LOGIN_FAIL
) : RootException(errorCode)