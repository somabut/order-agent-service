package com.orderagentservice.agent.util

import com.orderagentservice.agent.exception.AgentManyRequestException
import com.orderagentservice.agent.model.request.*
import com.orderagentservice.agent.model.response.GeminiResponse
import com.orderagentservice.agent.model.response.GptErrorResponse
import com.orderagentservice.agent.model.response.GptResponse
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import com.orderagentservice.order.exception.LlmParseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
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
    private val GEMINI_API_KEY = env.getProperty("agent.gemini.api-key")

    private val GPT_MODEL_NAME = env.getProperty("agent.openai.model-name")
    private val GPT_API_KEY = env.getProperty("agent.openai.api-key")

    fun queryGemini(prompt: String): String {
        //TODO(gpt 사용 제한으로 인한 일시적인 변경)
        return queryGpt(prompt)
//        return callOneGeminiApi(prompt)
//        return llmRateLimiter.executeWithLimit { apiKey ->
//            callGeminiApi(prompt, apiKey)
//        }
    }

    fun callOneGeminiApi(prompt: String): String {
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

    fun callGeminiApi(prompt: String, apiKey: String): String {
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

        val restTemplate = RestTemplate()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL_NAME:generateContent?key=$apiKey"

        try {
            val response: GeminiResponse = restTemplate.postForObject(url, request, GeminiResponse::class.java)!!
            val text = response.candidates[0].content.parts[0].text
            val json = text.replace("```json", "").replace("```", "").trim()
            return json
        } catch (e: HttpClientErrorException.TooManyRequests) {
            throw AgentManyRequestException()
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

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $GPT_API_KEY")
            set("Content-Type", "application/json")
        }

        val restTemplate = RestTemplate()
        val url = "https://api.openai.com/v1/chat/completions"

        val httpEntity = HttpEntity(request, headers)

        try {
            val response: GptResponse = restTemplate.postForObject(url, httpEntity, GptResponse::class.java)!!
            val text = response.choices[0].message.content
            val json = text.replace("```json", "").replace("```", "").trim()
            return json
        } catch (e: HttpClientErrorException) {
            if (e.statusCode.value() == 429) {
                handleRateLimitError(e)
                return queryGpt(prompt)
            }
            throw e
        }
    }

    private fun handleRateLimitError(e: HttpClientErrorException) {
        log.info("gpt 토큰 리미트에 걸렸습니다.")
        try {
            val errorResponse: GptErrorResponse = jsonMapper.readValue(e.responseBodyAsString, GptErrorResponse::class.java)
            val retryAfter = extractRetryAfterSeconds(errorResponse.error.message)

            log.info("${retryAfter}초 대기합니다.")
            Thread.sleep((retryAfter * 1000).toLong())
        } catch (ex: Exception) {
            log.info("Rate limit 에러 파싱 실패")
        }
    }

    private fun extractRetryAfterSeconds(message: String): Double {
        // "Please try again in 4.92s" 패턴에서 숫자 추출
        val pattern = Regex("try again in ([0-9.]+)s")
        return pattern.find(message)?.groupValues?.get(1)?.toDoubleOrNull() ?: 60.0
    }
}