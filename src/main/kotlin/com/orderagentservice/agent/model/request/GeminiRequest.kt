package com.orderagentservice.agent.model.request

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig?
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Double,
    val topP: Double,
    val maxOutputTokens: Int
)