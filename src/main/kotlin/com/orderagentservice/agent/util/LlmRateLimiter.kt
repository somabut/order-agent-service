package com.orderagentservice.agent.util

import com.orderagentservice.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class LlmRateLimiter @Autowired constructor(
    private val env: Environment
) {
    private val log = logger()

    private val apiKeys = listOf(
        env.getProperty("agent.gemini.api-key-1")!!,
        env.getProperty("agent.gemini.api-key-2")!!,
        env.getProperty("agent.gemini.api-key-3")!!,
        env.getProperty("agent.gemini.api-key-4")!!,
        env.getProperty("agent.gemini.api-key-5")!!,
    )

    // 각 API 키별 요청 시간 추적
    private val keyRequestTimes = ConcurrentHashMap<String, MutableList<Long>>()
    private val MAX_REQUEST_PER_MINUITE = 3
    private val ONE_MINUITE_SEC = 60_000L // 1분
    private var currentKeyIndex = AtomicInteger(0)

    init {
        // 각 API 키별 요청 시간 리스트 초기화
        apiKeys.forEach { key ->
            keyRequestTimes[key] = Collections.synchronizedList(mutableListOf())
        }
    }

    fun executeWithLimit(block: (apiKey: String) -> String): String {
        val availableKey = findAvailableKey()

        return if (availableKey != null) {
            keyRequestTimes[availableKey]?.add(System.currentTimeMillis())
            block(availableKey)
        } else {
            val waitTime = calculateWaitTime()
            log.info("gemini 제한으로 인해 대기 중입니다. 대기 시간: ${waitTime}ms")
            Thread.sleep(waitTime)
            executeWithLimit(block)
        }
    }

    private fun findAvailableKey(): String? {
        val currentTime = System.currentTimeMillis()

        // 라운드 로빈 방식으로 키 선택
        repeat(apiKeys.size) {
            val keyIndex = currentKeyIndex.getAndIncrement() % apiKeys.size
            val key = apiKeys[keyIndex]
            val requestTimes = keyRequestTimes[key] ?: return null

            // 1분 이전 요청 기록 제거
            requestTimes.removeAll { currentTime - it > ONE_MINUITE_SEC }

            // 해당 키로 요청 가능한지 확인
            if (requestTimes.size < MAX_REQUEST_PER_MINUITE) {
                return key
            }
        }

        return null
    }

    private fun calculateWaitTime(): Long {
        val currentTime = System.currentTimeMillis()
        var minWaitTime = Long.MAX_VALUE

        //최소 대기 시간 계산
        apiKeys.forEach { key ->
            val requestTimes = keyRequestTimes[key] ?: return@forEach
            if (requestTimes.size >= MAX_REQUEST_PER_MINUITE) {
                val oldestRequest = requestTimes.minOrNull()
                if (oldestRequest != null) {
                    val waitTime = ONE_MINUITE_SEC - (currentTime - oldestRequest)
                    if (waitTime > 0 && waitTime < minWaitTime) {
                        minWaitTime = waitTime
                    }
                }
            }
        }

        return if (minWaitTime == Long.MAX_VALUE) 0L else minWaitTime
    }
}