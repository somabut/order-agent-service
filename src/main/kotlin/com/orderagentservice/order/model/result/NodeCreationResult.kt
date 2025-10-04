package com.orderagentservice.order.model.result

import com.orderagentservice.order.model.dto.UiComponentParams

data class NodeCreationResult(
    val nodeId: String,
    val uiComponentParams: UiComponentParams,
)