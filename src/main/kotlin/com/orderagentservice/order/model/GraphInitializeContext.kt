package com.orderagentservice.order.model

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.order.model.entity.UiEntity

data class GraphInitializeContext(
    val kioskId: String,
    var determinePlace: Boolean,
    var lastNode: UiEntity?,
    var nowCategory: String?,
    var imageHash: String?,
    val history: MutableList<AgentActionDto>
)