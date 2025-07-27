package com.orderagentservice.mocktest.order

import com.orderagentservice.agent.PlaceAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.order.model.GraphInitializeContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.PlaceGraphInitializeService
import com.orderagentservice.order.service.UtgService
import com.orderagentservice.order.util.UiExtractorManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PlaceGraphInitializeServiceTest {
    companion object {
        private const val TEST_KIOSK_ID = "KIOSK_123123"
        private const val TEST_LAST_NODE_ID = "NODE_123"
        private const val TEST_ENTITY_ID = "ENTITY_123"
        private const val TEST_X_COORDINATE = 100
        private const val TEST_Y_COORDINATE = 200
        private const val TEST_TITLE = "포장"
        private val TEST_COORDINATE = CoordinateDto(TEST_X_COORDINATE, TEST_Y_COORDINATE, "coordinate")
    }

    private lateinit var placeAgent: PlaceAgent
    private lateinit var notificationService: NotificationService
    private lateinit var utgService: UtgService
    private lateinit var placeGraphInitializeService: PlaceGraphInitializeService
    private lateinit var uiExtractorManager: UiExtractorManager

    private lateinit var lastNode: UiEntity
    private lateinit var llmUiList: List<LlmUiComponentDto>
    private lateinit var uiEntity: UiEntity
    private lateinit var successAgentActionList: List<AgentActionDto>
    private lateinit var failAgentActionList: List<AgentActionDto>
    private lateinit var actionResult: Pair<Int, Int>

    @BeforeEach
    fun setUp() {
        placeAgent = mock()
        notificationService = mock()
        utgService = mock()
        uiExtractorManager = mock()
        placeGraphInitializeService = PlaceGraphInitializeService(placeAgent, notificationService, uiExtractorManager, utgService)

        lastNode = UiEntity(
            id = TEST_LAST_NODE_ID,
            isNext = false,
            x = 50, y = 50,
            title = "이전노드",
            kioskId = TEST_KIOSK_ID
        )

        llmUiList = listOf(
            LlmUiComponentDto(x = TEST_X_COORDINATE, y = TEST_X_COORDINATE, title = TEST_TITLE)
        )

        successAgentActionList = listOf(
            AgentActionDto(
                coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE), title = TEST_TITLE, goNext = true, score = 0.95F
            ),
            AgentActionDto(
                coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE), title = TEST_TITLE, goNext = true, score = 0.95F
            ),
        )

        failAgentActionList = listOf(
            AgentActionDto(
                coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE), title = TEST_TITLE, goNext = true, score = 0.95F
            ),
        )

        uiEntity = UiEntity(
            id = TEST_ENTITY_ID,
            isNext = false,
            x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE,
            title = TEST_TITLE,
            kioskId = TEST_KIOSK_ID
        )

        actionResult = Pair(TEST_X_COORDINATE, TEST_Y_COORDINATE)

        reset(placeAgent, notificationService, utgService)
    }

    @Test
    fun `포장_매장_UI가_발견되면_그래프_초기화가_성공한다`() {
        // given: 포장/매장 UI가 발견되는 상황
        val context = GraphInitializeContext(
            kioskId = TEST_KIOSK_ID,
            determinePlace = false,
            lowScoreCount = 0,
            lastNode = lastNode,
            imageHash = null,
            nowCategory = null,
            history = mutableListOf()
        )
        whenever(placeAgent.determineAction(llmUiList)).thenReturn(successAgentActionList)
        whenever(utgService.saveNode(any<UiDto>())).thenReturn(uiEntity)
        doNothing().whenever(utgService).saveRel(TEST_LAST_NODE_ID, TEST_ENTITY_ID, NodeRelation.HAS_TO)
        whenever(notificationService.sendActionCommand(TEST_KIOSK_ID, TEST_COORDINATE)).thenReturn(actionResult)

        // when: 그래프 초기화 실행
        placeGraphInitializeService.initializeGraph(context)

        // then: 히스토리가 정상적으로 반환되고 관련 메서드들이 호출된다
        assertEquals(2, context.history.size)

        verify(placeAgent).determineAction(llmUiList)
        verify(utgService, times(2)).saveNode(any())
        verify(utgService, times(2)).saveRel(TEST_LAST_NODE_ID, TEST_ENTITY_ID, NodeRelation.HAS_TO)
        verify(notificationService).sendActionCommand(anyString(), any<CoordinateDto>())
    }

    @Test
    fun `포장_매장_UI가_발견되지_않으면_빈_히스토리를_반환한다`() {
        // given: 포장/매장 UI가 발견되지 않는 상황
        val context = GraphInitializeContext(
            kioskId = TEST_KIOSK_ID,
            determinePlace = false,
            lowScoreCount = 0,
            lastNode = lastNode,
            imageHash = null,
            nowCategory = null,
            history = mutableListOf()
        )
        whenever(placeAgent.determineAction(llmUiList)).thenReturn(failAgentActionList)
        whenever(utgService.saveNode(any<UiDto>())).thenReturn(uiEntity)

        // when: 그래프 초기화 실행
        placeGraphInitializeService.initializeGraph(context)

        // then: 빈 히스토리가 반환되고 노드 생성 관련 메서드는 호출되지 않는다
        assertTrue(context.history.isEmpty())

        verify(placeAgent).determineAction(llmUiList)
        verify(utgService, never()).saveNode(any<UiDto>())
        verify(utgService, never()).saveRel(anyString(), anyString(), any())
        verify(notificationService, never()).sendActionCommand(anyString(), any<CoordinateDto>())
    }
}