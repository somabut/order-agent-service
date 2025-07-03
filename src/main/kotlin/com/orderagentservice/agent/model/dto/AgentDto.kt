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
)

data class AgentStepDto(
    val steps: List<String>
)