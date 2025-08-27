package com.orderagentservice.order.model.log

import com.orderagentservice.order.model.type.LogType
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.type.SaveNodeType
import com.orderagentservice.order.model.type.UtgType

data class UtgProcessLog(
    val type: LogType = LogType.UTG_PROCESS,
    val kioskId: String,
    val message: String,
)

data class UtgStartLog(
    val type: LogType = LogType.UTG_START,
    val utgType: UtgType,
    val kioskId: String,
)

data class UtgNowMenuLog(
    val type: LogType = LogType.UTG_NOW_MENU,
    val kioskId: String,
    val menu: String,
    val category: String
)

data class UtgEndLog(
    val type: LogType = LogType.UTG_END,
    val utgType: UtgType,
    val kioskId: String,
    val processingTime: Long,
    val totalTokenUsage: Int
)

data class MenuGetLog(
    val type: LogType = LogType.UTG_MENU_GET,
    val kioskId: String,
    val menus: List<MenuInfoDto>
)

data class NodeSaveLog(
    val type: LogType = LogType.UTG_NODE_SAVE,
    val kioskId: String,
    val nodeType: SaveNodeType,
    val x: Int,
    val y: Int,
    val title: String,
    val imageName: String
)

data class NodeRelLog(
    val type: LogType = LogType.UTG_NODE_REL,
    val kioskId: String,
    val sourceId: String,
    val targetId: String,
    val relType: String,
)

data class LLmQueryLog(
    val type: LogType = LogType.UTG_LLM_QUERY,
    val modelName: String,
    val tokenUsage: Int,
)