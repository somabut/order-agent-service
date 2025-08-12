package com.orderagentservice.order.model.dto

import com.orderagentservice.global.model.dto.PixelCoordinate

data class DetectorUiComponentDto(
    val contents: String,
    val bbox: BoundingBoxDto
)

data class BoundingBoxDto(
    val coordinate: PixelCoordinate,
    val width: Int,
    val height: Int
)