package com.orderagentservice.agent.util

import com.orderagentservice.agent.model.LlmProvider
import com.orderagentservice.agent.model.response.GptErrorResponse
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class LlmRateLimiter @Autowired constructor(
    private val env: Environment
) {
    private val log = logger()

    private val geminiApiKeys: List<String> = (1..5).mapNotNull { env.getProperty("agent.gemini.api-key-$it") }
    private val geminiKeyRequestTimes = ConcurrentHashMap<String, MutableList<Long>>()
    private val MAX_REQUEST_PER_MINUITE = 3
    private val ONE_MINUITE_SEC = 60_000L
    private val geminiCurrentKeyIndex = AtomicInteger(0)

    private val GPT_API_KEY = env.getProperty("agent.openai.api-key")!!

    init {
        // 각 API 키별 요청 시간 리스트 초기화
        geminiApiKeys.forEach { key ->
            geminiKeyRequestTimes[key] = Collections.synchronizedList(mutableListOf())
        }
    }

    fun <T> executeWithLimit(provider: LlmProvider, block: (apiKey: String) -> T): T {
        return when (provider) {
            LlmProvider.GEMINI -> executeForGemini(block)
            LlmProvider.GPT -> executeForGpt(block)
        }
    }

    private fun <T> executeForGemini(block: (apiKey: String) -> T): T {
        val availableKey = findAvailableGeminiKey()

        return if (availableKey != null) {
            geminiKeyRequestTimes[availableKey]?.add(System.currentTimeMillis())
            block(availableKey)
        } else {
            val waitTime = calculateGeminiWaitTime()
            log.info("Gemini API 제한으로 대기합니다. 예상 대기 시간: ${waitTime}ms")
            Thread.sleep(waitTime)
            executeForGemini(block) // 재귀 호출로 재시도
        }
    }

    private fun findAvailableGeminiKey(): String? {
        val currentTime = System.currentTimeMillis()
        repeat(geminiApiKeys.size) {
            val keyIndex = geminiCurrentKeyIndex.getAndIncrement() % geminiApiKeys.size
            val key = geminiApiKeys[keyIndex]
            val requestTimes = geminiKeyRequestTimes[key] ?: return@repeat

            requestTimes.removeAll { currentTime - it > ONE_MINUITE_SEC }
            if (requestTimes.size < MAX_REQUEST_PER_MINUITE) {
                return key
            }
        }
        return null
    }

    private fun calculateGeminiWaitTime(): Long {
        val currentTime = System.currentTimeMillis()
        var minWaitTime = Long.MAX_VALUE

        geminiApiKeys.forEach { key ->
            val requestTimes = geminiKeyRequestTimes[key] ?: return@forEach
            if (requestTimes.size >= MAX_REQUEST_PER_MINUITE) {
                val oldestRequest = requestTimes.minOrNull() ?: return@forEach
                val waitTime = ONE_MINUITE_SEC - (currentTime - oldestRequest)
                if (waitTime in 1..minWaitTime) {
                    minWaitTime = waitTime
                }
            }
        }
        return if (minWaitTime == Long.MAX_VALUE) ONE_MINUITE_SEC else minWaitTime + 100
    }

    private fun <T> executeForGpt(block: (apiKey: String) -> T): T {
        while (true) {
            try {
                // GPT는 단일 API 키를 사용하므로 바로 block을 실행합니다.
                return block(GPT_API_KEY)
            } catch (e: HttpClientErrorException) {
                // 429 Too Many Requests 에러가 아니면 예외를 다시 던집니다.
                if (e.statusCode.value() != 429) {
                    throw e
                }
                // Rate Limit 에러를 처리하고 루프를 계속하여 재시도합니다.
                handleGptRateLimitError(e)
            }
        }
    }

    private fun handleGptRateLimitError(e: HttpClientErrorException) {
        log.info("GPT API rate limit에 도달했습니다.")
        try {
            val errorResponse: GptErrorResponse = jsonMapper.readValue(e.responseBodyAsString, GptErrorResponse::class.java)
            val retryAfter = extractGptRetryAfterSeconds(errorResponse.error.message)
            log.info("GPT API 대기: ${retryAfter}초 후 재시도합니다.")
            Thread.sleep((retryAfter * 1000).toLong())
        } catch (ex: Exception) {
            log.warn("GPT Rate limit 에러 메시지 파싱 실패. 기본값인 60초 대기합니다.", ex)
            Thread.sleep(60_000)
        }
    }

    private fun extractGptRetryAfterSeconds(message: String): Double {
        val pattern = Regex("try again in ([0-9.]+)s")
        return pattern.find(message)?.groupValues?.get(1)?.toDoubleOrNull() ?: 60.0
    }
}