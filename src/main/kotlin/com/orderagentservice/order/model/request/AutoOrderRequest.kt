package com.orderagentservice.order.model.request

data class AutoOrderRequest(
    val autoOrderMenus: List<AutoOrderMenu>,
    val place: String?,
    val payment: String
)

data class AutoOrderMenu(
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