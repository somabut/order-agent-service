package com.orderagentservice.order.model.request

import com.orderagentservice.order.model.dto.MenuInfoDto

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

    fun toMenuInfoDto() = MenuInfoDto(
        title = title,
        options = autoOrderOptions.map { it.title },
        category = category,
    )
}

data class AutoOrderOption(
    val title: String,
    val count: Int
)