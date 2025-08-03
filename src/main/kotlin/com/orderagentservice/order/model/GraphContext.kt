package com.orderagentservice.order.model

import com.orderagentservice.agent.model.dto.AgentActionDto

data class GraphContext(
    val kioskId: String,
    var isPlaceDetermined: Boolean,
    var lastNodeId: String?,
    var stationNodeId: String?,
    var currentCategory: String?,
    val history: MutableList<AgentActionDto>
) {
    companion object {
        fun toBasicContext(kioskId: String) = GraphContext(
            kioskId = kioskId,
            isPlaceDetermined = false,
            lastNodeId = null,
            stationNodeId = null,
            currentCategory = null,
            history = mutableListOf<AgentActionDto>()
        )
    }
}