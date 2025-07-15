package com.orderagentservice.order.model.dto

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.order.model.entity.UiEntity

data class MenuGraphDto(
    val actionList: List<AgentActionDto>,
    val lastNode: UiEntity,
    val isFindPlace: Boolean
)