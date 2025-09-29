package com.orderagentservice.agent.model.response

class LlmResponse(
    val content: String,
    val usage: Int
) {
    companion object {}
}