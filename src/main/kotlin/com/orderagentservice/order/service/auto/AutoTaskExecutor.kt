package com.orderagentservice.order.service.auto

import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderOption

interface AutoTaskExecutor {
    fun clickMenu(context: AutoOrderContext, menu: AutoOrderMenu): ActionPathDto
    fun clickOption(context: AutoOrderContext, options: List<AutoOrderOption>, menuNodeId: String): String
    fun clickBack(context: AutoOrderContext, menuNodeId: String): String
    fun clickPayment(context: AutoOrderContext, paymentNode: ActionPathDto)
    fun clickPlace(context: AutoOrderContext): Boolean
}