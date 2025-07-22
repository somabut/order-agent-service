package com.orderagentservice.order.model.request

import com.orderagentservice.jsonMapper

data class AutoOrderRequest(
    val autoOrderMenus: List<AutoOrderMenu>,
    val place: String?,
    val payment: String
) {
}

data class AutoOrderMenu(
    val category: String,
    val title: String,
    val count: Int,
    val autoOrderOptions: List<AutoOrderOption>
) {
    override fun toString(): String = "{\"title\": \"$title\", \"count\": \"$count\"}"
}

data class AutoOrderOption(
    val title: String,
    val count: Int
)