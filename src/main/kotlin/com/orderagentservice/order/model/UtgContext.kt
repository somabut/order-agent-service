package com.orderagentservice.order.model

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.order.model.type.LogicType

data class UtgContext(
    val logicState: LogicType,

    val kioskId: String,
    var lastNodeId: String?,
    var stationNodeId: String?,
    var screenNodeId: String,

    var isPlaceDetermined: Boolean,
    var currentCategory: String?,
    var imageName: String,
    var menuBackUi: String?,

    val history: MutableList<AgentActionDto>,
    val pushedImages: MutableList<String>
) {
    companion object {
        fun toBasicContext(kioskId: String, logicState: LogicType = LogicType.INITIALIZE) = UtgContext(
            logicState = logicState,
            kioskId = kioskId,
            isPlaceDetermined = false,
            lastNodeId = null,
            stationNodeId = null,
            currentCategory = null,
            history = mutableListOf(),
            imageName = "",
            screenNodeId = "",
            menuBackUi = null,
            pushedImages = mutableListOf()
        )
    }
}