package com.orderagentservice.order.model.log

import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.type.LogType
import com.orderagentservice.order.model.type.NodeType

data class OrderStartLog(
    val type: LogType = LogType.AO_START,
    val kioskId: String,
    val menus: String,
)

data class OrderProcessLog(
    val type: LogType = LogType.AO_PROCESS,
    val kioskId: String,
    val nodeType: NodeType,
    val title: String
)

data class OrderEndLog(
    val type: LogType = LogType.AO_END,
    val kioskId: String,
    val menus: List<MenuInfoDto>,
    val processingTime: Long,
)