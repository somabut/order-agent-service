package com.orderagentservice.agent.model.request

import com.fasterxml.jackson.annotation.JsonProperty

data class ClaudRequest(
    val model: String,
    @JsonProperty("max_tokens")
    val maxTokens: Int,
    val messages: List<ClaudMessage>
)

data class ClaudMessage(
    val role: String,
    val content: String
)