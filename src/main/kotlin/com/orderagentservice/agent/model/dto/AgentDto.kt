package com.orderagentservice.agent.model.dto

data class AgentActionDto (
    val goNext: Boolean,
    val score: Float,
    val coordinate: List<Int>,
    val bbox: List<Int>,
    val title: String
)

data class AgentBackDto(
    val score: Float,
    val coordinate: List<Int>,
    val bbox: List<Int>,
    val title: String
) {
    fun toActionDto() = AgentActionDto(
        goNext = false,
        score = score,
        coordinate = coordinate,
        bbox = bbox,
        title = title
    )
}

data class AgentPlaceDto(
    val goNext: Boolean,
    val score: Float,
    val coordinate: List<Int>,
    val bbox: List<Int>,
    val origin: String,
    val title: String
) {
    fun toActionDto() = AgentActionDto(
        goNext = false,
        score = score,
        coordinate = coordinate,
        bbox = bbox,
        title = title
    )
}