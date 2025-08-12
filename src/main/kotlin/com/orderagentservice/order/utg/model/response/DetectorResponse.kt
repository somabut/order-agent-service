package com.orderagentservice.order.utg.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.orderagentservice.order.utg.model.dto.DetectorUiComponentDto

data class DetectorResponse(
    @JsonProperty("ui_elements")
    val uiComponents: List<DetectorUiComponentDto>
)