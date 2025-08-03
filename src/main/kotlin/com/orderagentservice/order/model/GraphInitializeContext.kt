package com.orderagentservice.order.model

import com.orderagentservice.agent.model.dto.AgentActionDto

data class GraphInitializeContext(
    val kioskId: String,
    var isPlaceDetermined: Boolean,
    var lastNodeId: String?,
    var stationNodeId: String?,
    var currentCategory: String?,
    val history: MutableList<AgentActionDto>
)