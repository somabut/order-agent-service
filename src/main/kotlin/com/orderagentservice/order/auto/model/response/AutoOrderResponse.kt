package com.orderagentservice.order.auto.model.response

import com.orderagentservice.order.auto.model.AutoOrderResultDto

data class AutoOrderResponse(
    val taskId: String,
    val history: AutoOrderResultDto
)