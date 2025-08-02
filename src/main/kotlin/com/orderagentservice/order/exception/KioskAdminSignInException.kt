package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class KioskAdminSignInException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_ADMIN_LOGIN_FAIL
) : OrderAgentException(errorCode)