package com.orderagentservice.order.model

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentBackDto

data class GraphContext(
    val kioskId: String,
    var lastNodeId: String?,
    var stationNodeId: String?,
    var screenNodeId: String,

    var isPlaceDetermined: Boolean,
    var currentCategory: String?,
    var imageName: String,
    var menuBackUi: String?,

    val history: MutableList<AgentActionDto>,
) {
    companion object {
        fun toBasicContext(kioskId: String) = GraphContext(
            kioskId = kioskId,
            isPlaceDetermined = false,
            lastNodeId = null,
            stationNodeId = null,
            currentCategory = null,
            history = mutableListOf<AgentActionDto>(),
            imageName = "",
            screenNodeId = "",
            menuBackUi = null
        )
    }
}