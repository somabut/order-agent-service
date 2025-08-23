package com.orderagentservice.agent.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class ClaudResponse(
    val id: String,
    val type: String,
    val role: String?,
    val content: List<ClaudContent>?,
    val error: ErrorDetails? = null,
    val model: String?,

    @JsonProperty("stop_reason")
    val stopReason: String?,

    @JsonProperty("stop_sequence")
    val stopSequence: String? = null,
    val usage: Usage?
)

data class ClaudContent(
    val type: String,
    val text: String
)

data class Usage(
    @JsonProperty("input_tokens")
    val inputTokens: Int,

    @JsonProperty("output_tokens")
    val outputTokens: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ErrorDetails(
    val type: String,
    val message: String
)