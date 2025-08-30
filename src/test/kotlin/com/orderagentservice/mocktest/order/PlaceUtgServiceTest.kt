package com.orderagentservice.mocktest.order

import com.orderagentservice.agent.PlaceAgent
import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import com.orderagentservice.order.service.graph.GraphServiceImpl
import com.orderagentservice.order.service.utg.UiDetectorManager
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
class PlaceUtgServiceTest {
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
    private lateinit var graphService: GraphServiceImpl
    private lateinit var placeUtgService: PlaceUtgService
    private lateinit var uiDetectorManager: UiDetectorManager
    private lateinit var usageTracker: UsageTracker
    private lateinit var logService: LogService

    private lateinit var lastNode: UiEntity
    private lateinit var llmUiList: List<UiComponentDto>
    private lateinit var uiEntity: UiEntity
    private lateinit var successAgentActionList: List<AgentActionDto>
    private lateinit var failAgentActionList: List<AgentActionDto>
    private lateinit var actionResult: CoordinateDto

    @BeforeEach
    fun setUp() {
        placeAgent = mock()
        notificationService = mock()
        graphService = mock()
        uiDetectorManager = mock()
        placeUtgService = PlaceUtgService(
            placeAgent = placeAgent,
            notificationService = notificationService,
            uiDetectorManager = uiDetectorManager,
            graphService = graphService,
            logService = logService,
        )

        lastNode = UiEntity(
            id = TEST_LAST_NODE_ID,
            isNext = false,
            x = 50, y = 50,
            title = "이전노드",
            kioskId = TEST_KIOSK_ID
        )

        llmUiList = listOf(
            UiComponentDto(x = TEST_X_COORDINATE, y = TEST_X_COORDINATE, title = TEST_TITLE)
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

        reset(placeAgent, notificationService, graphService)
    }

    @Test
    fun `포장_매장_UI가_발견되면_그래프_초기화가_성공한다`() {
        // given: 포장/매장 UI가 발견되는 상황
        val context = GraphContext(
            kioskId = TEST_KIOSK_ID,
            isPlaceDetermined = false,
            stationNodeId = null,
            lastNodeId = lastNode.id,
            currentCategory = null,
            history = mutableListOf(),
            imageName = ""
        )
        whenever(placeAgent.determineAction(llmUiList)).thenReturn(successAgentActionList)
        whenever(graphService.saveNode(any<UiDto>())).thenReturn(uiEntity)
        doNothing().whenever(graphService).saveRel(TEST_LAST_NODE_ID, TEST_ENTITY_ID, NodeRelationType.HAS_TO)
        whenever(notificationService.sendActionCommand(TEST_KIOSK_ID, TEST_COORDINATE)).thenReturn(actionResult)

        // when: 그래프 초기화 실행
        placeUtgService.initializeGraph(context)

        // then: 히스토리가 정상적으로 반환되고 관련 메서드들이 호출된다
        assertEquals(2, context.history.size)

        verify(placeAgent).determineAction(llmUiList)
        verify(graphService, times(2)).saveNode(any())
        verify(graphService, times(2)).saveRel(TEST_LAST_NODE_ID, TEST_ENTITY_ID, NodeRelationType.HAS_TO)
        verify(notificationService).sendActionCommand(anyString(), any<CoordinateDto>())
    }

    @Test
    fun `포장_매장_UI가_발견되지_않으면_빈_히스토리를_반환한다`() {
        // given: 포장/매장 UI가 발견되지 않는 상황
        val context = GraphContext(
            kioskId = TEST_KIOSK_ID,
            isPlaceDetermined = false,
            lastNodeId = lastNode.id,
            stationNodeId = null,
            currentCategory = null,
            history = mutableListOf(),
            imageName = ""
        )
        whenever(placeAgent.determineAction(llmUiList)).thenReturn(failAgentActionList)
        whenever(graphService.saveNode(any<UiDto>())).thenReturn(uiEntity)

        // when: 그래프 초기화 실행
        placeUtgService.initializeGraph(context)

        // then: 빈 히스토리가 반환되고 노드 생성 관련 메서드는 호출되지 않는다
        assertTrue(context.history.isEmpty())

        verify(placeAgent).determineAction(llmUiList)
        verify(graphService, never()).saveNode(any<UiDto>())
        verify(graphService, never()).saveRel(anyString(), anyString(), any())
        verify(notificationService, never()).sendActionCommand(anyString(), any<CoordinateDto>())
    }
}