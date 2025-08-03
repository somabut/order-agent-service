package com.orderagentservice.order.model.dto

data class ActionPathDto(
    val id: String,
    val title: String,
    val x: Int,
    val y: Int
) {
    companion object {
        fun toPathDtoList(nodes: List<Map<String, Any>>): List<ActionPathDto> {
            return nodes
                .map { node -> ActionPathDto(
                    node["id"].toString(), node["title"].toString(),
                    x = (node["x"] as Number).toInt(), y = (node["y"] as Number).toInt()
                ) }
        }
    }

}