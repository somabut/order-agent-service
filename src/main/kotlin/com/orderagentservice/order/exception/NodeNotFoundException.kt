package com.orderagentservice.order.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class NodeNotFoundException(
    override val errorCode: ErrorCode = ErrorCode.ORDER_NO_NODE
) : OrderAgentException(errorCode)