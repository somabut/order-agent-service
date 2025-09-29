package com.orderagentservice.agent.util

import com.orderagentservice.agent.exception.AgentManyRequestException
import com.orderagentservice.agent.exception.LlmServerOverLoadException
import com.orderagentservice.agent.model.LlmProvider
import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.request.*
import com.orderagentservice.agent.model.response.ClaudResponse
import com.orderagentservice.agent.model.response.GeminiResponse
import com.orderagentservice.agent.model.response.GptResponse
import com.orderagentservice.agent.model.response.LlmResponse
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.core.env.getProperty
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class LlmManager @Autowired constructor(
    private val env: Environment,
    private val llmRateLimiter: LlmRateLimiter,
){
    private val log = logger()

//    private val GEMINI_MODEL_NAME = env.getProperty("agent.gemini.model-name")
//    private val GPT_MODEL_NAME = env.getProperty("agent.openai.model-name")
    private val CLAUD_MODEL_NAME = env.getProperty("agent.claud.model-name")

    fun query(prompt: String): LlmResponse {
        return queryClaud(prompt)
    }

    fun queryClaud(prompt: String, waitTime: Long = 2): LlmResponse {
        val request = ClaudRequest(
            model = CLAUD_MODEL_NAME!!,
            maxTokens = 2048,
            messages = listOf(
                ClaudMessage(
                    role = "user",
                    content = prompt
                )
            )
        )

        val response = llmRateLimiter.executeWithLimit(LlmProvider.CLAUD) { apiKey ->
            val restTemplate = RestTemplate()
            val headers = HttpHeaders()

            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("x-api-key", apiKey)
            headers.set("anthropic-version", "2023-06-01")

            val url = "https://api.anthropic.com/v1/messages"
            val httpEntity = HttpEntity(request, headers)
            restTemplate.postForObject(url, httpEntity, ClaudResponse::class.java)!!
        }

        val text = response.content?.get(0)!!.text
        val usage = response.usage!!.inputTokens + response.usage.outputTokens
        log.info("$CLAUD_MODEL_NAME: [$usage]의 토큰을 사용했습니다.")

        val json = text.replace("```json", "").replace("```", "").trim()
        return LlmResponse(json, usage)
    }
}