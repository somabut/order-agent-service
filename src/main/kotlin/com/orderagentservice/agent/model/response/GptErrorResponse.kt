package com.orderagentservice.agent.model.response

data class GptErrorResponse(
    val error: OpenAIError
)

data class OpenAIError(
    val message: String,
    val type: String,
    val param: String?,
    val code: String?
)