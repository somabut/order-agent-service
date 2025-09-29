package com.orderagentservice.unit.agent

import com.fasterxml.jackson.core.JsonProcessingException
import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.agent.model.response.LlmResponse
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.exception.LlmParseException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class BackAgentTest {
    @MockK
    private lateinit var llmManager: LlmManager

    @MockK(relaxed = true)
    private lateinit var usageTracker: UsageTracker

    private lateinit var backAgent: BackAgent

    private val MOCK_AGENT_BACK_DTO_1 = AgentBackDto(1.0F, listOf(90, 456), listOf(66, 446, 114, 466), "카트 담기")
    private val MOCK_AGENT_BACK_DTO_2 = AgentBackDto(0.9F, listOf(120, 74), listOf(36, 64, 204, 84), "다음으로")
    private val MOCK_AGENT_BACK_JSON_1 = jsonMapper.writeValueAsString(MOCK_AGENT_BACK_DTO_1)
    private val MOCK_AGENT_BACK_JSON_2 = jsonMapper.writeValueAsString(MOCK_AGENT_BACK_DTO_2)
    private val MOCK_AGENT_BACK_USAGE_1 = 10
    private val MOCK_AGENT_BACK_USAGE_2 = 20

    private val MOCK_MALFORMED_JSON = """{"goNext": "true", "title": "잘못된 JSON" """
    private val MOCK_MALFORMED_USAGE = 2

    private val MOCK_UI_LIST = listOf<UiComponentDto>()

    @BeforeEach
    fun setUp() {
        backAgent = BackAgent(llmManager, usageTracker)
    }

    @Test
    @DisplayName("성공 케이스: LLM이 유효한 응답들을 반환할 때, 가장 빈도가 높은 결과를 정확히 반환하고 사용량을 업데이트해야 한다.")
    fun `determineAction should return the most frequent response and update usage`() {
        //given
        val llmResponses = listOf(
            LlmResponse(content = MOCK_AGENT_BACK_JSON_1, usage = MOCK_AGENT_BACK_USAGE_1),
            LlmResponse(content = MOCK_AGENT_BACK_JSON_1, usage = MOCK_AGENT_BACK_USAGE_1),
            LlmResponse(content = MOCK_AGENT_BACK_JSON_1, usage = MOCK_AGENT_BACK_USAGE_1),
            LlmResponse(content = MOCK_AGENT_BACK_JSON_2, usage = MOCK_AGENT_BACK_USAGE_2),
            LlmResponse(content = MOCK_AGENT_BACK_JSON_2, usage = MOCK_AGENT_BACK_USAGE_2)
        )
        coEvery { llmManager.query(any()) } returnsMany llmResponses
        every { usageTracker.totalUsage } returns 0

        // when
        val result = backAgent.determineAction(MOCK_UI_LIST)

        // then
        assertEquals(MOCK_AGENT_BACK_DTO_1.toString(), result.toString())

        coVerify(exactly = 5) { llmManager.query(any()) }
        verify { usageTracker.totalUsage += 70 }
    }

    @Test
    @DisplayName("예외 케이스: LLM이 파싱할 수 없는 JSON을 반환할 때, JsonProcessingException이 발생해야 한다.")
    fun `determineAction should throw JsonProcessingException on malformed json`() {
        //given
        val llmResponse = LlmResponse(content = MOCK_MALFORMED_JSON, usage = MOCK_MALFORMED_USAGE)
        coEvery { llmManager.query(any()) } returns llmResponse

        // when & then
        assertThrows<LlmParseException> {
            backAgent.determineAction(MOCK_UI_LIST)
        }
    }
}