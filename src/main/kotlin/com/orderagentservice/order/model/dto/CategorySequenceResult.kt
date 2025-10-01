package com.orderagentservice.order.model.dto

import com.orderagentservice.agent.model.dto.UiComponentDto

data class CategorySequenceResult (
    val uiList: List<UiComponentDto>,
    val categoryScreenId: String
)