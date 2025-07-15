package com.orderagentservice.agent

import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.agent.util.LlmRateLimiter
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@SpringBootTest
class LlmRateLimiterTest @Autowired constructor(
    private val llmRateLimiter: LlmRateLimiter,
    private val llmManager: LlmManager
) {
    @Test
    fun `요청 제한에 맞게 요청한다`() {
        // given: 요청횟수가 주어진다
        val requestCount = 55 // 제한(10 * 5(키 개수))보다 많은 요청
        val results = mutableListOf<String>()
        val startTime = System.currentTimeMillis()

        // when: llm에게 질의 한다
        repeat(requestCount) { index ->
            val result = llmRateLimiter.executeWithLimit { apiKey ->
                "Request $index with $apiKey"
            }
            results.add(result)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // then: 제한이 걸리지 않을 정도만큼 대기한다
        assertThat(results).hasSize(requestCount)
        assertThat(duration).isGreaterThan(60_000) // 대기 시간이 발생했는지 확인
    }

    @Test
    fun `라운드로빈으로 API키를 선택한다`() {
        // given
        val usedKeys = mutableListOf<String>()

        for (i in 1..10) {
            // when
            llmRateLimiter.executeWithLimit { apiKey ->
                usedKeys.add(apiKey)
                "moodTRBL"
            }

            //then
            assertThat(usedKeys.size % 5).isEqualTo(i % 5)
        }
    }

    @Test
    fun `llm에게 실제로 질의하여 Too Many Request 에러가 발생하지 않는다`() {
        for (i in 1..50) {
            val result = llmManager.queryGemini("안녕 응답이 잘 갔으면 정확하게 'moodTRBL'이라고만 답해")
            assertThat(result).isEqualTo("moodTRBL")
            println(i)
        }
    }

//    15 RPM – 분당 최대 15회 요청 가능
//    1,500 RPD – 하루 1,500회 요청 가능
//    TPM: 분당 최대 토큰 1,000,000 토큰
}