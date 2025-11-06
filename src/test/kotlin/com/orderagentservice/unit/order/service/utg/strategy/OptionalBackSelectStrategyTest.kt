package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.UiComponentDto
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
import com.orderagentservice.order.service.utg.strategy.OptionalBackSelectStrategy
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class OptionalBackSelectStrategyTest {
    @MockK
    private lateinit var notificationService: NotificationService
    @MockK private lateinit var uiNodeIntegrator: UiNodeIntegrator
    @MockK private lateinit var screenNodeIntegrator: ScreenNodeIntegrator
    @MockK private lateinit var comparatorManager: ComparatorManager
    @MockK private lateinit var backAgent: BackAgent
    @MockK private lateinit var logService: LogService

    private lateinit var strategy: OptionalBackSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = OptionalBackSelectStrategy(
            notificationService,
            uiNodeIntegrator,
            screenNodeIntegrator,
            comparatorManager,
            backAgent,
            logService
        )
    }

    @Test
    @DisplayName("execute: hasOption이 true일 때 navigateBack을 호출 (캐시 미스 케이스)")
    fun `execute should call navigateBack when hasOption is true`() {
        // given
        val context =  spyk(
            UtgContext(
                kioskId = "kiosk-123", screenNodeId = "screen-001",
                menuBackUi = null, logicState = LogicType.INITIALIZE, isPlaceDetermined = true,
                imageName = "imageName", history = mutableListOf(), pushedImages = mutableListOf(),
                lastNodeId = "", stationNodeId = "", currentCategory = ""
            )
        )
        val menuNodeId = "menu-abc"
        val newNodeId = "node-xyz-new"
        val uiList = listOf(mockk<UiComponentDto>())

        val mockBackAction = AgentBackDto(title = "뒤로가기", coordinate = listOf(10, 20), score = 1.0f, bbox = listOf(5, 15, 15, 25))
        val mockCreationResult = mockk<NodeCreationResult>()
        val mockCoordinate = mockk<CoordinateDto>()
        val mockUiComponentParams = mockk<UiComponentParams>()

        every { backAgent.determineAction(uiList) } returns mockBackAction
        every { uiNodeIntegrator.integrateBackNode(any(), menuNodeId, context) } returns mockCreationResult
        every { mockCreationResult.nodeId } returns newNodeId
        every { mockCreationResult.uiComponentParams } returns mockUiComponentParams
        every { logService.printLog(any<UtgProcessLog>()) } just runs

        every { screenNodeIntegrator.linkNode(any(), any(), any(), any()) } just runs
        coEvery { notificationService.sendActionCommand(any(), any()) } returns mockCoordinate


        // when
        val result = strategy.execute(context, menuNodeId, uiList, hasOption = true)

        // then
        assertEquals(newNodeId, result)
        verify(exactly = 1) { backAgent.determineAction(uiList) }
    }

    @Test
    @DisplayName("execute: hasOption이 false일 때 빈 문자열을 반환하고 navigateBack 미호출")
    fun `execute should return empty string and not call navigateBack when hasOption is false`() {
        // given
        val context = spyk(
            UtgContext(
                kioskId = "kiosk-123", screenNodeId = "screen-001",
                menuBackUi = null, logicState = LogicType.INITIALIZE, isPlaceDetermined = true,
                imageName = "imageName", history = mutableListOf(), pushedImages = mutableListOf(),
                lastNodeId = "", stationNodeId = "", currentCategory = ""
            )
        )
        val menuNodeId = "menu-abc"
        val uiList = listOf(mockk<UiComponentDto>())

        // when
        val result = strategy.execute(context, menuNodeId, uiList, hasOption = false)

        // then
        assertEquals("", result)

        verify(exactly = 0) { backAgent.determineAction(any()) }
        verify(exactly = 0) { comparatorManager.wordCompare(any(), any()) }
        verify(exactly = 0) { uiNodeIntegrator.integrateBackNode(any(), any(), any()) }
        verify(exactly = 0) { screenNodeIntegrator.linkNode(any(), any(), any(), any()) }
    }
}