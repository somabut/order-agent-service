package com.orderagentservice.agent.model.dto

data class AgentActionDto (
    val goNext: Boolean,
    val score: Float,
    val coordinate: List<Int>,
    val title: String
)

data class AgentBackDto(
    val score: Float,
    val coordinate: List<Int>,
    val title: String
) {
    fun toActionDto() = AgentActionDto(
        goNext = false,
        score = score,
        coordinate = coordinate,
        title = title
    )
}

data class AgentPageDto(
    val score: Float,
    val contain: Boolean
)

data class AgentStepDto(
    val steps: List<String>
)