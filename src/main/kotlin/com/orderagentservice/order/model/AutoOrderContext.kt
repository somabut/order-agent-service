package com.orderagentservice.order.model

import com.orderagentservice.order.model.dto.MenuInfoDto

data class AutoOrderContext(
    val kioskId: String,
    val taskId: String,
    var nodeId: String,
    val place: String?,
    var isPlaceSelected: Boolean,
    val history: AutoOrderResultDto
) {
    companion object {
        fun toBasicContext(
            kioskId: String, taskId: String, nodeId: String,
            place: String? = null
        ) = AutoOrderContext(
            kioskId = kioskId,
            taskId = taskId,
            nodeId = nodeId,
            place = place,
            isPlaceSelected = (place == null),
            history = AutoOrderResultDto(
                menus = mutableListOf(),
                place = null,
                payment = "카드결제",
            )
        )
    }
}

data class AutoOrderResultDto(
    val menus: MutableList<MenuInfoDto>,
    var place: String?,
    var payment: String?,
    var processingTime: Long = 0
)