package com.orderagentservice.order.model.request

import com.orderagentservice.order.model.type.CommandType

data class CommandRequest<T> (
    val kioskId: String,
    val commandId: String,
    val commandType: CommandType,
    val data: T?
)