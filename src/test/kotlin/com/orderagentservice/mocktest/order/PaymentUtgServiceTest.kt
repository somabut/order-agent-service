package com.orderagentservice.mocktest.order

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.type.ExtractType
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.payment.PaymentUtgService
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import com.orderagentservice.order.service.graph.ui.UiGraphServiceImpl
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.payment.PaymentNavigator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import kotlin.test.Test

@SpringBootTest
class PaymentUtgServiceTest {
    companion object {
        private const val TEST_KIOSK_ID = "KIOSK_123123"
        private const val TEST_LAST_NODE_ID = "NODE_123"
        private const val TEST_ENTITY_ID = "ENTITY_123"
        private const val TEST_COMPLETE_ENTITY_ID = "COMPLETE_ENTITY_123"
        private const val TEST_X_COORDINATE = 100
        private const val TEST_Y_COORDINATE = 200
        private const val TEST_TITLE = "결제하기"
        private val TEST_IMAGE_DATA = File("")
    }

    private lateinit var paymentAgent: PaymentAgent
    private lateinit var paymentNavigator: PaymentNavigator
    private lateinit var placeUtgService: PlaceUtgService
    private lateinit var notificationService: NotificationService
    private lateinit var uiDetectorManager: UiDetectorManager
    private lateinit var graphService: UiGraphServiceImpl
    private lateinit var paymentUtgService: PaymentUtgService
    private lateinit var usageTracker: UsageTracker
    private lateinit var logService: LogService

    private lateinit var lastNode: UiEntity
    private lateinit var llmUiList: MutableList<UiComponentDto>
    private lateinit var agentActionDto: AgentActionDto
    private lateinit var uiEntity: UiEntity
    private lateinit var completeEntity: UiEntity
    private lateinit var placeActionList: List<AgentActionDto>

    @BeforeEach
    fun setUp() {
        paymentAgent = mock()
        placeUtgService = mock()
        notificationService = mock()
        uiDetectorManager = mock()
        graphService = mock()
        usageTracker = mock()
        logService = mock()
        paymentUtgService = PaymentUtgService(
            paymentNavigator = paymentNavigator,
            logService = logService,
            usageTracker = usageTracker,
        )

        lastNode = UiEntity(
            id = TEST_LAST_NODE_ID,
            isNext = false,
            x = 50,
            y = 50,
            title = "이전노드",
            kioskId = TEST_KIOSK_ID
        )

        llmUiList = mutableListOf(
            UiComponentDto(
                x = TEST_X_COORDINATE, y = TEST_X_COORDINATE, title = TEST_TITLE
            )
        )

        agentActionDto = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = TEST_TITLE,
            goNext = false,
            score = 0.95F
        )

        uiEntity = UiEntity(
            id = TEST_ENTITY_ID,
            isNext = false,
            x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE,
            title = TEST_TITLE,
            kioskId = TEST_KIOSK_ID
        )

        completeEntity = UiEntity(
            id = TEST_COMPLETE_ENTITY_ID,
            isNext = false,
            x = -1, y = -1,
            title = "complete",
            kioskId = TEST_KIOSK_ID
        )

        placeActionList = listOf(
            AgentActionDto(false, 0.9F, listOf(150, 250), "포장"),
            AgentActionDto(false, 0.8F, listOf(100, 200), "매장")
        )

        reset(paymentAgent, placeUtgService, notificationService, uiDetectorManager, graphService)
    }

    @Test
    fun `포장_매장_UI를_찾은_상태에서_결제_그래프_초기화가_성공한다`() {
        // given: 포장/매장 UI를 이미 찾은 상태
        val context = GraphContext(
            kioskId = TEST_KIOSK_ID,
            isPlaceDetermined = true,
            lastNodeId = lastNode.id,
            stationNodeId = null,
            currentCategory = null,
            history = mutableListOf(),
            imageName = "",
            screenNodeId = ""
        )
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(any())
        whenever(uiDetectorManager.getUiComponents(context, ExtractType.SOM)).thenReturn(llmUiList)
        whenever(paymentAgent.determineAction(llmUiList)).thenReturn(agentActionDto)
        whenever(graphService.saveNode(any<UiDto>())).thenReturn(uiEntity).thenReturn(completeEntity)
        doNothing().whenever(graphService).saveRel(anyString(), anyString(), any())

        // when: 결제 그래프 초기화 실행

        paymentUtgService.initializeGraph(context)

        // then: 결제 액션이 히스토리에 추가되고 완료 노드가 생성된다
        assertEquals(1, context.history.size)
        assertEquals(agentActionDto, context.history[0])

        verify(placeUtgService, never()).initializeGraph(any())
        verify(notificationService).sendCaptureCommand(TEST_KIOSK_ID)
        verify(uiDetectorManager).getUiComponents(any(), ExtractType.SOM)
        verify(paymentAgent).determineAction(llmUiList)
        verify(graphService, times(2)).saveNode(any<UiDto>())
        verify(graphService, times(2)).saveRel(anyString(), anyString(), any<NodeRelationType>())
    }

    @Test
    fun `포장_매장_UI를_찾지_못한_상태에서_결제_그래프_초기화가_성공한다`() {
        // given: 포장/매장 UI를 찾지 못한 상태
        val context = GraphContext(
            kioskId = TEST_KIOSK_ID,
            isPlaceDetermined = false,
            stationNodeId = lastNode.id,
            lastNodeId = null,
            currentCategory = null,
            history = mutableListOf(),
            imageName = "",
            screenNodeId = ""
        )
        whenever(placeUtgService.initializeGraph(any())).thenAnswer {
            placeActionList.forEach { context.history.add(it) }
        }
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(any())
        whenever(uiDetectorManager.getUiComponents(context, ExtractType.SOM)).thenReturn(llmUiList)
        whenever(paymentAgent.determineAction(llmUiList)).thenReturn(agentActionDto)
        whenever(graphService.saveNode(any<UiDto>())).thenReturn(uiEntity).thenReturn(completeEntity)
        doNothing().whenever(graphService).saveRel(anyString(), anyString(), any())

        // when: 결제 그래프 초기화 실행
        paymentUtgService.initializeGraph(context)

        // then: 포장/매장 액션과 결제 액션이 모두 히스토리에 추가된다
        assertEquals(3, context.history.size)
        assertEquals(placeActionList[0], context.history[0])
        assertEquals(placeActionList[1], context.history[1])
        assertEquals(agentActionDto, context.history[2])

        verify(placeUtgService).initializeGraph(any())
        verify(notificationService).sendCaptureCommand(TEST_KIOSK_ID)
        verify(uiDetectorManager).getUiComponents(any(), ExtractType.SOM)
        verify(paymentAgent).determineAction(llmUiList)
        verify(graphService, times(2)).saveNode(any<UiDto>())
        verify(graphService, times(2)).saveRel(anyString(), anyString(), any<NodeRelationType>())
    }

    @Test
    fun `여러_단계의_결제_과정을_거쳐_그래프가_완성된다`() {
        // given: 여러 단계의 결제 과정이 필요한 상황
        val isFindPlace = true
        val firstAction = AgentActionDto(
            coordinate = listOf(100, 200),
            title = "카드결제",
            goNext = true,
            score = 0.9F
        )
        val secondAction = AgentActionDto(
            coordinate = listOf(200, 300),
            title = "결제완료",
            goNext = false,
            score = 0.95F
        )
        val firstEntity = UiEntity(TEST_ENTITY_ID, null, true, 100, 200, "카드결제", TEST_KIOSK_ID)
        val secondEntity = UiEntity("ENTITY_456", null, false, 200, 300, "결제완료", TEST_KIOSK_ID)

        val context = GraphContext(
            kioskId = TEST_KIOSK_ID,
            isPlaceDetermined = true,
            stationNodeId = null,
            lastNodeId = lastNode.id,
            currentCategory = null,
            history = mutableListOf(),
            imageName = "",
            screenNodeId = ""
        )

        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(any())
        whenever(uiDetectorManager.getUiComponents(context, ExtractType.SOM)).thenReturn(llmUiList)
        whenever(paymentAgent.determineAction(llmUiList)).thenReturn(firstAction).thenReturn(secondAction)
        whenever(graphService.saveNode(any<UiDto>()))
            .thenReturn(firstEntity)
            .thenReturn(secondEntity)
            .thenReturn(completeEntity)
        doNothing().whenever(graphService).saveRel(anyString(), anyString(), any())

        // when: 결제 그래프 초기화 실행

        paymentUtgService.initializeGraph(context)

        // then: 모든 결제 단계가 히스토리에 추가되고 완료 노드가 생성된다
        assertEquals(2, context.history.size)
        assertEquals(firstAction, context.history[0])
        assertEquals(secondAction, context.history[1])

        verify(notificationService, times(2)).sendCaptureCommand(TEST_KIOSK_ID)
        verify(uiDetectorManager, times(2)).getUiComponents(any(), ExtractType.SOM)
        verify(paymentAgent, times(2)).determineAction(llmUiList)
        verify(graphService, times(3)).saveNode(any<UiDto>())
        verify(graphService, times(3)).saveRel(anyString(), anyString(), any<NodeRelationType>())
    }

    @Test
    fun 완료_노드가_올바르게_생성된다() {
        // given: 결제 과정이 완료되는 상황
        val context = GraphContext(
            kioskId = TEST_KIOSK_ID,
            isPlaceDetermined = true,
            stationNodeId = null,
            lastNodeId = lastNode.id,
            currentCategory = null,
            history = mutableListOf(),
            imageName = "",
            screenNodeId = ""
        )
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(any())
        whenever(uiDetectorManager.getUiComponents(context, ExtractType.SOM)).thenReturn(llmUiList)
        whenever(paymentAgent.determineAction(llmUiList)).thenReturn(agentActionDto)
        whenever(graphService.saveNode(any<UiDto>())).thenReturn(uiEntity).thenReturn(completeEntity)
        doNothing().whenever(graphService).saveRel(anyString(), anyString(), any())

        // when: 결제 그래프 초기화 실행

        paymentUtgService.initializeGraph(context)

        // then: 완료 노드가 올바른 파라미터로 생성된다
        val expectedCompleteUiDto = UiDto(
            isNext = false,
            x = -1, y = -1,
            title = "complete",
            kioskId = TEST_KIOSK_ID
        )

        val captor = argumentCaptor<UiDto>()
        verify(graphService, times(2)).saveNode(captor.capture())

        val lastArg = captor.lastValue
        assertEquals(expectedCompleteUiDto.x, lastArg.x)
        assertEquals(expectedCompleteUiDto.y, lastArg.y)
        assertEquals(expectedCompleteUiDto.title, lastArg.title)
        assertEquals(expectedCompleteUiDto.kioskId, lastArg.kioskId)

        verify(graphService, times(2)).saveRel(anyString(), anyString(), any<NodeRelationType>())
    }
}