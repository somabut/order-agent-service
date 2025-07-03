package com.orderagentservice.agent.util

import com.orderagentservice.agent.exception.AgentManyRequestException
import com.orderagentservice.agent.model.request.Content
import com.orderagentservice.agent.model.request.GeminiRequest
import com.orderagentservice.agent.model.request.Part
import com.orderagentservice.agent.model.response.GeminiResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class LlmManager @Autowired constructor(
    private val env: Environment
){
    private val GEMINI_MODEL_NAME = env.getProperty("agent.gemini.model-name")
    private val GEMINI_API_KEY = env.getProperty("agent.gemini.api-key")

    fun queryGeminiModel(prompt: String): String {
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt)
                    )
                )
            )
        )

        val restTemplate = RestTemplate()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL_NAME:generateContent?key=$GEMINI_API_KEY"

        try {
            val response: GeminiResponse = restTemplate.postForObject(url, request, GeminiResponse::class.java)!!
            val text = response.candidates[0].content.parts[0].text
            val json = text.replace("```json", "").replace("```", "").trim()
            return json
        } catch (e: HttpClientErrorException.TooManyRequests) {
            throw AgentManyRequestException()
        }
    }
}