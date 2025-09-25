package com.orderagentservice.order.model.response

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto

data class ImageCompareResponse(
    val result: Boolean,
)

data class WordCompareResponse(
    val result: WordMatchDto
)