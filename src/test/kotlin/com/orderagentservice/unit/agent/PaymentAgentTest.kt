package com.orderagentservice.unit.agent

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentActionDto
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
class PaymentAgentTest {
    @MockK
    private lateinit var llmManager: LlmManager

    @MockK(relaxed = true)
    private lateinit var usageTracker: UsageTracker

    private lateinit var paymentAgent: PaymentAgent

    private val MOCK_AGENT_PAYMENT_DTO = AgentActionDto(false, 1.0F, listOf(66, 446), listOf(66, 64, 204, 84), "결제")
    private val MOCK_AGENT_PAYMENT_JSON = jsonMapper.writeValueAsString(MOCK_AGENT_PAYMENT_DTO)
    private val MOCK_AGENT_PAYMENT_USAGE = 10

    private val MOCK_MALFORMED_JSON = """{"goNext": "true", "title": "잘못된 JSON" """
    private val MOCK_MALFORMED_USAGE = 2

    private val MOCK_UI_LIST = listOf<UiComponentDto>()

    @BeforeEach
    fun setUp() {
        paymentAgent = PaymentAgent(llmManager, usageTracker)
    }

    @Test
    @DisplayName("성공 케이스: LLM이 유효한 JSON을 반환할 때, 응답을 DTO로 파싱하고 사용량을 업데이트해야 한다.")
    fun `determineAction should parse response and update usage on valid LLM output`() {
        //given
        val llmResponse = LlmResponse(content = MOCK_AGENT_PAYMENT_JSON, usage = MOCK_AGENT_PAYMENT_USAGE)
        coEvery { llmManager.query(any()) } returns llmResponse
        every { usageTracker.totalUsage } returns 0

        //when
        val result = paymentAgent.determineAction(MOCK_UI_LIST)

        //then
        assertEquals(MOCK_AGENT_PAYMENT_DTO.toString(), result.toString())

        coVerify(exactly = 1) { llmManager.query(any()) }
        verify { usageTracker.totalUsage += 10 }
    }

    @Test
    @DisplayName("예외 케이스: LLM이 파싱할 수 없는 JSON을 반환할 때, JsonProcessingException이 발생해야 한다.")
    fun `determineAction should throw JsonProcessingException on malformed json`() {
        //given
        val llmResponse = LlmResponse(content = MOCK_MALFORMED_JSON, usage = MOCK_MALFORMED_USAGE)
        coEvery { llmManager.query(any()) } returns llmResponse

        // when & then
        assertThrows<LlmParseException> {
            paymentAgent.determineAction(MOCK_UI_LIST)
        }
    }
}