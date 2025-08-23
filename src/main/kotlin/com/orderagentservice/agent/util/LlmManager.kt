package com.orderagentservice.agent.util

import com.orderagentservice.agent.exception.AgentManyRequestException
import com.orderagentservice.agent.exception.LlmServerOverLoadException
import com.orderagentservice.agent.model.LlmProvider
import com.orderagentservice.agent.model.request.*
import com.orderagentservice.agent.model.response.ClaudResponse
import com.orderagentservice.agent.model.response.GeminiResponse
import com.orderagentservice.agent.model.response.GptResponse
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
    private val llmRateLimiter: LlmRateLimiter
){
    private val log = logger()

    private val GEMINI_MODEL_NAME = env.getProperty("agent.gemini.model-name")
    private val GPT_MODEL_NAME = env.getProperty("agent.openai.model-name")
    private val CLAUD_MODEL_NAME = env.getProperty("agent.claud.model-name")

    private val CLAUD_API_KEY = env.getProperty("agent.claud.api-key")

    fun query(prompt: String): String {
        return queryClaud(prompt)
//        return queryGpt(prompt)
//        return llmRateLimiter.executeWithLimit { apiKey ->
//            callGeminiApi(prompt, apiKey)
//        }
    }

    fun queryGemini(prompt: String): String {
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt)
                    )
                )
            ),
            GenerationConfig(
                temperature = 0.2,
                topP = 1.0,
                maxOutputTokens = 256
            )
        )
        
        return llmRateLimiter.executeWithLimit(LlmProvider.GEMINI) { apiKey ->
            val restTemplate = RestTemplate()
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL_NAME:generateContent?key=$apiKey"

            try {
                val response: GeminiResponse = restTemplate.postForObject(url, request, GeminiResponse::class.java)!!
                val text = response.candidates[0].content.parts[0].text
                val json = text.replace("```json", "").replace("```", "").trim()
                json
            } catch (e: HttpClientErrorException.TooManyRequests) {
                throw AgentManyRequestException()
            }
        }
    }

    fun queryGpt(prompt: String): String {
        val request = GptRequest(
            model = GPT_MODEL_NAME!!,
            messages = listOf(
                Message(
                    role = "user",
                    content = prompt
                )
            )
        )

        return llmRateLimiter.executeWithLimit(LlmProvider.GPT) { apiKey ->
            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer $apiKey")
                set("Content-Type", "application/json")
            }

            val restTemplate = RestTemplate()
            val url = "https://api.openai.com/v1/chat/completions"
            val httpEntity = HttpEntity(request, headers)
            val response = restTemplate.postForObject(url, httpEntity, GptResponse::class.java)!!
            val text = response.choices[0].message.content
            val json = text.replace("```json", "").replace("```", "").trim()

            json
        }
    }

    fun queryClaud(prompt: String, waitTime: Long = 2): String {
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

        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("x-api-key", CLAUD_API_KEY)
        headers.set("anthropic-version", "2023-06-01")
        val url = "https://api.anthropic.com/v1/messages"

        val httpEntity = HttpEntity(request, headers)
        val response = restTemplate.postForObject(url, httpEntity, ClaudResponse::class.java)!!

        if (response.type == "error" && response.error!!.type == "overloaded_error") {
            if (waitTime >= 10L) {
                throw LlmServerOverLoadException()
            }
            log.info("엔트로픽 서버 과부화 ${waitTime}초 대기합니다.")
            Thread.sleep(waitTime)
            queryClaud(prompt, waitTime * 2)
        }

        val text = response.content?.get(0)!!.text
        val json = text.replace("```json", "").replace("```", "").trim()
        return json
    }
}