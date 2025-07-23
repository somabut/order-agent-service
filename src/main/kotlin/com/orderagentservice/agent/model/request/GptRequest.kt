package com.orderagentservice.agent.model.request

data class GptRequest(
    val model: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

