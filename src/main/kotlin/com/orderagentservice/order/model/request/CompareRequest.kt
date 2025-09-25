package com.orderagentservice.order.model.request

import com.orderagentservice.agent.model.dto.UiComponentDto

data class WordCompareRequest(
    val target: String,
    val candidates: List<UiComponentDto>,
)