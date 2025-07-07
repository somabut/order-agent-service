package com.orderagentservice.agent

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
    private val llmRateLimiter: LlmRateLimiter
) {
    @Test
    fun `요청 제한에 맞게 요청한다`() {
        // given
        val requestCount = 55 // 제한(10)보다 많은 요청
        val results = mutableListOf<String>()
        val startTime = System.currentTimeMillis()

        // when
        repeat(requestCount) { index ->
            val result = llmRateLimiter.executeWithLimit { apiKey ->
                "Request $index with $apiKey"
            }
            results.add(result)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // then
        assertThat(results).hasSize(requestCount)
        assertThat(duration).isGreaterThan(0) // 대기 시간이 발생했는지 확인
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
}