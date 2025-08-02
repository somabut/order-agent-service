package com.orderagentservice.order.model

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.order.model.entity.UiEntity

data class GraphInitializeContext(
    val kioskId: String,
    var determinePlace: Boolean,
    var lastNodeId: String?,
    var stationNodeId: String?,
    var nowCategory: String?,
    val history: MutableList<AgentActionDto>
)