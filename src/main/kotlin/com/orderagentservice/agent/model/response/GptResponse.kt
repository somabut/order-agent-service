package com.orderagentservice.agent.model.response

data class GptResponse(
    val choices: List<Choice>
)

data class Choice(
    val index: Int,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)