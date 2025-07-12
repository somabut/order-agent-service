package com.orderagentservice.order.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.orderagentservice.order.model.dto.OmniUiComponentDto

data class OmniResponse(
    @JsonProperty("ui_elements")
    val uiComponents: List<OmniUiComponentDto>
)