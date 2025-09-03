package com.orderagentservice.order.model.dto

import com.orderagentservice.agent.model.dto.UiComponentDto

data class AllUiComponentDto (
    val uiElements: List<UiComponentDto>,
    val ocrElements: List<UiComponentDto>,
    val yoloElements: List<UiComponentDto>
)