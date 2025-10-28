package com.orderagentservice.unit.agent.util

import com.orderagentservice.agent.model.LlmProvider
import com.orderagentservice.agent.util.LlmRateLimiter
import com.orderagentservice.global.util.Sleeper
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.HttpServerErrorException
import java.util.concurrent.atomic.AtomicInteger

@ExtendWith(MockKExtension::class)
class LlmRateLimiterTest {
    @MockK
    private lateinit var env: Environment

    @MockK
    private lateinit var sleeper: Sleeper

    private lateinit var llmRateLimiter: LlmRateLimiter

    private val MOCK_API_KEY = "TEST_API_KEY"
    private val LLM_JSON = """
        {
            "score": 1.0,
            "coordinate": [90, 456],
            "bbox":[66, 446, 114, 466],
            "title": "카트 담기"
        }
    """.trimIndent()
    private val OVERLOAD_HTTP_ERROR = HttpServerErrorException(HttpStatusCode.valueOf(529), "Overloaded")

    @BeforeEach
    fun setUp() {
        every { env.getProperty("agent.claud.api-key") } returns MOCK_API_KEY
        llmRateLimiter = LlmRateLimiter(env, sleeper)
    }

    @Test
    @DisplayName("첫 시도에 성공하면, 블록을 한 번만 실행하고 결과를 즉시 반환한다")
    fun `executeWithLimit should succeed on the first try`() {
        // given
        val block = mockk<(String) -> String>()
        every { block.invoke(MOCK_API_KEY) } returns LLM_JSON

        // when
        val actualResult = llmRateLimiter.executeWithLimit(LlmProvider.CLAUD, block)

        // then
        assertThat(actualResult).isEqualTo(LLM_JSON)
        verify(exactly = 1) { block.invoke(MOCK_API_KEY) }
    }

    @Test
    @DisplayName("서버 과부하(529)가 아닌 다른 서버 에러 발생 시, 재시도 없이 즉시 예외를 던진다")
    fun `executeWithLimit should throw exception immediately on non-overload server error`() {
        // given
        val block = mockk<(String) -> String>()
        val internalServerError = HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        every { block.invoke(any()) } throws internalServerError

        // when & then
        val exception = assertThrows<HttpServerErrorException> {
            llmRateLimiter.executeWithLimit(LlmProvider.CLAUD, block)
        }
        assertThat(exception.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        verify(exactly = 1) { block.invoke(any()) }
    }

    @Test
    @DisplayName("서버 과부하(529) 발생 시, 한 번 재시도한 후 성공한다")
    fun `executeWithLimit should succeed after one retry on overload error`() {
        // given
        var callCount = AtomicInteger(0)
        val block = mockk<(String) -> String>()

        every { block.invoke(any()) } answers {
            callCount.incrementAndGet()
            if (callCount.get() <= 3) {
                throw OVERLOAD_HTTP_ERROR
            } else {
                LLM_JSON
            }
        }

        // when & then
        every { sleeper.sleep(any<Long>()) } just Runs
        val actualResult = llmRateLimiter.executeWithLimit(LlmProvider.CLAUD, block)

        assertThat(actualResult).isEqualTo(LLM_JSON)
        verify(exactly = 4) { block.invoke(MOCK_API_KEY) }
        verify(exactly = 1) { sleeper.sleep(2000L) }
        verify(exactly = 1) { sleeper.sleep(4000L) }
        verify(exactly = 1) { sleeper.sleep(8000L) }
    }
}