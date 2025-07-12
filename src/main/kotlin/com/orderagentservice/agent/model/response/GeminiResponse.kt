package com.orderagentservice.agent.model.response

data class GeminiResponse (
    val candidates: List<Candidate>
)

data class Candidate (
    val content: Content,
    val finishReason: String
)

data class Content (
    val parts: List<Parts>,
    val role: String
)

data class Parts (
    val text: String
)