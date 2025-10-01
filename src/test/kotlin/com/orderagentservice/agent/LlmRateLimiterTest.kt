package com.orderagentservice.agent

import com.orderagentservice.agent.exception.LlmServerOverLoadException
import com.orderagentservice.agent.model.LlmProvider
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.agent.util.LlmRateLimiter
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.HttpServerErrorException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class LlmRateLimiterTest @Autowired constructor(
    private val llmRateLimiter: LlmRateLimiter,
    private val llmManager: LlmManager
) {

    @Test
    fun `llm에게 실제로 질의하여 Too Many Request 에러가 발생하지 않는다`() {
        for (i in 1..50) {
            val result = llmManager.query("안녕 응답이 잘 갔으면 정확하게 'moodTRBL'이라고만 답해")
            assertThat(result).isEqualTo("moodTRBL")
            println(i)
        }
    }

    @Test
    fun `gpt rate limit에 걸리는 경우 대기 시간을 정확히 추출한다`() {
        // given
        val message = "Rate limit reached for gpt-4-0613 in organization org-53KmE07Lhp6JYsn71TCeMbjq on tokens per min (TPM): Limit 10000, Used 9348, Requested 1472. Please try again in 4.92s. Visit https://platform.openai.com/account/rate-limits to learn more."
        val method = LlmManager::class.java.getDeclaredMethod("extractRetryAfterSeconds", String::class.java)
        method.isAccessible = true

        // when
        val result = method.invoke(llmManager, message) as Double

        // then
        assertThat(result).isEqualTo(4.92)
    }

    @Test
    fun `엔트로픽 529에러에 걸리는 경우 한번 더 요청한다`() {
        val callCount = AtomicInteger(0)
        val mockApiCall: (String) -> String = mock()

        `when`(mockApiCall.invoke(anyString())).thenAnswer {
            val count = callCount.incrementAndGet()
            if (count <= 3) {
                throw HttpServerErrorException(HttpStatusCode.valueOf(529), "Service Overloaded")
            }
            "성공 응답"
        }

        val result = executeForClaudTest(mockApiCall)
        verify(mockApiCall, times(4)).invoke(anyString())
        assertThat(callCount.get()).isEqualTo(4)
        assertThat(result).isEqualTo("성공 응답")
    }

    // 테스트용 executeForClaud 메서드
    private fun <T> executeForClaudTest(block: (String) -> T): T {
        var waitTime = 2L
        val maxWaitTime = 16L

        while (true) {
            try {
                return block("test_api_key")
            } catch (e: HttpServerErrorException) {
                if (e.statusCode.value() != 529) {
                    throw e
                }

                if (waitTime > maxWaitTime) {
                    throw LlmServerOverLoadException()
                }

                println("엔트로픽 서버 과부화로 인해 ${waitTime}초 대기합니다.")
                waitTime *= 2
            }
        }
    }
}