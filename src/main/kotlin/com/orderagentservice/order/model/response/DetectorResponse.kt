package com.orderagentservice.order.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.orderagentservice.order.model.dto.DetectorUiComponentDto

data class DetectorResponse(
    @JsonProperty("ui_elements")
    val uiComponents: List<DetectorUiComponentDto>,

    @JsonProperty("ocr_elements")
    val ocrComponents: List<DetectorUiComponentDto>,

    @JsonProperty("yolo_elements")
    val yoloComponents: List<DetectorUiComponentDto>,
)