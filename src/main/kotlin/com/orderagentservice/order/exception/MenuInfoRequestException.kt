package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class MenuInfoRequestException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_MENU_REQUEST_FAIL
) : RootException(errorCode)