package com.orderagentservice.order.model.response

import com.orderagentservice.order.model.AutoOrderResultDto

data class AutoOrderResponse(
    val taskId: String,
    val history: AutoOrderResultDto
)