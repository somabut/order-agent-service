package com.orderagentservice.unit.agent.util

import com.orderagentservice.agent.model.LlmProvider
import com.orderagentservice.agent.model.response.ClaudContent
import com.orderagentservice.agent.model.response.ClaudResponse
import com.orderagentservice.agent.model.response.Usage
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.agent.util.LlmRateLimiter
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.env.Environment

@ExtendWith(MockKExtension::class)
class LlmManagerTest {
    @MockK
    private lateinit var env: Environment

    @MockK
    private lateinit var llmRateLimiter: LlmRateLimiter

    private lateinit var llmManager: LlmManager

    private val LLM_JSON = """
        {
            "score": 1.0,
            "coordinate": [90, 456],
            "bbox":[66, 446, 114, 466],
            "title": "카트 담기"
        }
    """.trimIndent()
    private val LLM_TOTAL_USAGE = 200
    private val LLM_INPUT_USAGE = 100
    private val LLM_OUTPUT_USAGE = 100
    private val CLAUD_MODEL = "TEST_CLAUD_MODEL"
    private val OPENAI_MODEL = "TEST_OPENAI_MODEL"
    private val GEMINI_MODEL = "TEST_GEMINI_MODEL"

    @BeforeEach
    fun setUp() {
        every { env.getProperty("agent.gemini.model-name") } returns GEMINI_MODEL
        every { env.getProperty("agent.openai.model-name") } returns OPENAI_MODEL
        every { env.getProperty("agent.claud.model-name") } returns CLAUD_MODEL

        llmManager = LlmManager(env, llmRateLimiter)
    }

    @Test
    @DisplayName("queryClaud 호출 성공 시, API 응답을 정제하여 LlmResponse를 올바르게 반환한다")
    fun `queryClaud should return parsed LlmResponse on successful execution`() {
        // given
        val prompt = "이것은 테스트 프롬프트입니다."

        val mockApiResponse: ClaudResponse = ClaudResponse.toMock()

        every { llmRateLimiter.executeWithLimit(
            eq(LlmProvider.CLAUD),
            ofType<(String) -> ClaudResponse>())
        } returns mockApiResponse

        // when
        val result = llmManager.queryClaud(prompt)

        // then
        assertThat(result.content).isEqualTo(LLM_JSON)
        assertThat(result.usage).isEqualTo(LLM_TOTAL_USAGE)

        verify(exactly = 1) { env.getProperty("agent.claud.model-name") }
        verify(exactly = 1) { llmRateLimiter.executeWithLimit(LlmProvider.CLAUD, any()) }
    }

    private fun ClaudResponse.Companion.toMock() = ClaudResponse(
        id = "TEST", role = null,
        content = listOf(ClaudContent(
            type = "TEST",
            text = LLM_JSON
        )),
        error = null, type = "TEST", model = CLAUD_MODEL,
        stopReason = null, stopSequence = null,
        usage = Usage(
            inputTokens = LLM_INPUT_USAGE, outputTokens = LLM_OUTPUT_USAGE
        )
    )
}