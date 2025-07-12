package com.orderagentservice.order.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.orderagentservice.global.model.dto.RatioCoordinate

data class OmniUiComponentDto(
    val contents: List<String>,
    val bbox: BoundingBoxDto
)

data class BoundingBoxDto(
    val coordinate: RatioCoordinate,
    val width: Int,
    val height: Int
)