package com.orderagentservice.unit.agent

import com.orderagentservice.agent.PlaceAgent
import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentPlaceDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.agent.model.response.LlmResponse
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PlaceAgentTest {
    @MockK
    private lateinit var llmManager: LlmManager

    @MockK(relaxed = true)
    private lateinit var usageTracker: UsageTracker

    private lateinit var placeAgent: PlaceAgent

    private val MOCK_AGENT_PLACE_TAKEOUT_DTO = AgentPlaceDto(false, 1.0F, listOf(66, 446), listOf(66, 64, 204, 84), "포장", "포장")
    private val MOCK_AGENT_PLACE_TAKEIN_DTO = AgentPlaceDto(false, 1.0F, listOf(66, 446), listOf(66, 64, 204, 84), "매장", "매장")
    private val MOCK_AGENT_PLACE_TAKEOUT_JSON = jsonMapper.writeValueAsString(MOCK_AGENT_PLACE_TAKEOUT_DTO)
    private val MOCK_AGENT_PLACE_TAKEIN_JSON = jsonMapper.writeValueAsString(MOCK_AGENT_PLACE_TAKEIN_DTO)
    private val MOCK_AGENT_PLACE_JSON = """[${MOCK_AGENT_PLACE_TAKEOUT_JSON}, ${MOCK_AGENT_PLACE_TAKEIN_JSON}]""".trimIndent()
    private val MOCK_AGENT_PLACE_USAGE = 10

    private val MOCK_MALFORMED_JSON = """{"goNext": "true", "title": "잘못된 JSON" """
    private val MOCK_MALFORMED_USAGE = 2

    private val MOCK_UI_LIST = listOf<UiComponentDto>()

    @BeforeEach
    fun setUp() {
        placeAgent = PlaceAgent(llmManager, usageTracker)
    }

    @Test
    @DisplayName("성공 케이스: LLM이 '매장'과 '포장' UI를 모두 찾은 경우, 두 개의 DTO 리스트를 반환해야 한다.")
    fun `determineAction should return list of two DTOs when both dine-in and take-out are found`() {
        // given
        coEvery { llmManager.query(any()) } returns LlmResponse(content = MOCK_AGENT_PLACE_JSON, usage = MOCK_AGENT_PLACE_USAGE)

        // when
        val actualDtoList = placeAgent.determineAction(emptyList())

        // then
        assertEquals(2, actualDtoList.size)
        assertEquals(MOCK_AGENT_PLACE_TAKEOUT_DTO.toString(), actualDtoList[0].toString())
        assertEquals(MOCK_AGENT_PLACE_TAKEIN_DTO.toString(), actualDtoList[1].toString())
        coVerify(exactly = 1) { llmManager.query(any()) }
        verify { usageTracker.totalUsage += MOCK_AGENT_PLACE_USAGE }
    }
}