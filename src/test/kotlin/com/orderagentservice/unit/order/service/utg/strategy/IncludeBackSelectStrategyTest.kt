package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.result.NodeCreationResult
import com.orderagentservice.order.model.type.LogicType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.menu.UiNodeIntegrator
import com.orderagentservice.order.service.utg.strategy.IncludeBackSelectStrategy
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import kotlin.collections.emptyList
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class IncludeBackSelectStrategyTest {
    // Mocks for all dependencies
    @MockK
    private lateinit var notificationService: NotificationService
    @MockK
    private lateinit var uiNodeIntegrator: UiNodeIntegrator
    @MockK
    private lateinit var screenNodeIntegrator: ScreenNodeIntegrator
    @MockK
    private lateinit var comparatorManager: ComparatorManager
    @MockK
    private lateinit var backAgent: BackAgent
    @MockK
    private lateinit var logService: LogService

    private lateinit var strategy: IncludeBackSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = IncludeBackSelectStrategy(
            notificationService,
            uiNodeIntegrator,
            screenNodeIntegrator,
            comparatorManager,
            backAgent,
            logService
        )
    }

    @Test
    @DisplayName("execute: 캐시 미스 (context.menuBackUi가 null) 시 backAgent를 호출하고 노드 ID 반환")
    fun `execute should call navigateBack and use backAgent on cache miss`() {
        // given
        val kioskId = "kiosk-123"
        val screenNodeId = "screen-001"
        val menuNodeId = "menu-abc"
        val newNodeId = "node-xyz-new"
        val cachedBackTitle = "캐시된_뒤로가기"
        val uiList = listOf(mockk<UiComponentDto>())

        val context = spyk(
            UtgContext(
                kioskId = kioskId, screenNodeId = screenNodeId,
                menuBackUi = null, logicState = LogicType.INITIALIZE, isPlaceDetermined = true,
                imageName = "imageName", history = mutableListOf(), pushedImages = mutableListOf(),
                lastNodeId = "", stationNodeId = "", currentCategory = ""
            )
        )

        val mockBackAction = AgentBackDto(
            title = "뒤로가기",
            coordinate = listOf(10, 20),
            score = 1.0f,
            bbox = listOf(5, 15, 15, 25)
        )
        val mockCreationResult = mockk<NodeCreationResult>()
        val coordinateSlot = slot<CoordinateDto>()
        val mockCoordinate = mockk<CoordinateDto>()
        val mockUiComponentParams = mockk<UiComponentParams>()

        every { backAgent.determineAction(uiList) } returns mockBackAction

        every { uiNodeIntegrator.integrateBackNode(any(), menuNodeId, context) } returns mockCreationResult
        every { mockCreationResult.nodeId } returns newNodeId
        every { mockCreationResult.uiComponentParams } returns mockUiComponentParams

        every { screenNodeIntegrator.linkNode(any(), any(), any(), any()) } just runs
        every { logService.printLog(any<UtgProcessLog>()) } just runs
        coEvery { notificationService.sendActionCommand(kioskId, capture(coordinateSlot)) } returns mockCoordinate
        val result = strategy.execute(context, menuNodeId, uiList, hasOption = true)

        // then
        assertEquals(newNodeId, result)

        verify(exactly = 1) { backAgent.determineAction(uiList) }
        verify(exactly = 0) { comparatorManager.wordCompare(any(), any()) }

        verify(exactly = 1) { uiNodeIntegrator.integrateBackNode(any(), menuNodeId, context) }
        verify(exactly = 1) { screenNodeIntegrator.linkNode(kioskId, newNodeId, screenNodeId, any()) }
        verify(exactly = 1) { logService.printLog(any<UtgProcessLog>()) }

        assertEquals(mockBackAction.title, context.menuBackUi)

        assertEquals(10, coordinateSlot.captured.x)
        assertEquals(20, coordinateSlot.captured.y)
    }

    @Test
    @DisplayName("execute: 캐시 히트 (context.menuBackUi 존재) 시 comparatorManager를 호출하고 노드 ID 반환")
    fun `execute should call navigateBack and use comparatorManager on cache hit`() {
        // given
        val kioskId = "kiosk-123"
        val screenNodeId = "screen-001"
        val menuNodeId = "menu-abc"
        val newNodeId = "node-456-hit"
        val cachedBackTitle = "캐시된_뒤로가기"
        val uiList = listOf(mockk<UiComponentDto>())

        val context = spyk(
            UtgContext(
                kioskId = kioskId, screenNodeId = screenNodeId,
                menuBackUi = cachedBackTitle, logicState = LogicType.INITIALIZE, isPlaceDetermined = true,
                imageName = "imageName", history = mutableListOf(), pushedImages = mutableListOf(),
                lastNodeId = "", stationNodeId = "", currentCategory = ""
            )
        )

        val mockMatchDto = WordMatchDto(
            title = cachedBackTitle,
            x = 50, y = 60,
            minX = 45, minY = 55, maxX = 55, maxY = 65,
            score = 1.0
        )
        val mockCreationResult = mockk<NodeCreationResult>()
        val coordinateSlot = slot<CoordinateDto>()
        val mockCoordinate = mockk<CoordinateDto>()
        val mockUiComponentParams = mockk<UiComponentParams>()

        // 1. cacheBackUi (cache hit)
        every { comparatorManager.wordCompare(cachedBackTitle, uiList) } returns mockMatchDto

        // 2. navigateBack
        every { uiNodeIntegrator.integrateBackNode(any(), menuNodeId, context) } returns mockCreationResult
        every { mockCreationResult.nodeId } returns newNodeId
        every { mockCreationResult.uiComponentParams } returns mockUiComponentParams

        every { screenNodeIntegrator.linkNode(any(), any(), any(), any()) } just runs
        every { logService.printLog(any<UtgProcessLog>()) } just runs
        coEvery { notificationService.sendActionCommand(kioskId, capture(coordinateSlot)) } returns mockCoordinate
        // when
        val result = strategy.execute(context, menuNodeId, uiList, hasOption = false)

        // then
        assertEquals(newNodeId, result)

        verify(exactly = 0) { backAgent.determineAction(any()) }
        verify(exactly = 1) { comparatorManager.wordCompare(cachedBackTitle, uiList) }

        verify(exactly = 1) { uiNodeIntegrator.integrateBackNode(any(), menuNodeId, context) }
        verify(exactly = 1) { screenNodeIntegrator.linkNode(kioskId, newNodeId, screenNodeId, any()) }
        verify(exactly = 1) { logService.printLog(any<UtgProcessLog>()) }

        assertEquals(mockMatchDto.x, coordinateSlot.captured.x)
        assertEquals(mockMatchDto.y, coordinateSlot.captured.y)
    }
}