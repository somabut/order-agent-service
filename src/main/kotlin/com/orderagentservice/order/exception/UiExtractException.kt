package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class UiExtractException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_UI_EXTRACT_FAIL
) : OrderAgentException(errorCode)