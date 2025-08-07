package com.orderagentservice.order.model.dto

import com.orderagentservice.global.model.dto.PixelCoordinate
import com.orderagentservice.global.model.dto.RatioCoordinate

data class DetectorUiComponentDto(
    val contents: String,
    val bbox: BoundingBoxDto
)

data class BoundingBoxDto(
    val coordinate: PixelCoordinate,
    val width: Int,
    val height: Int
)