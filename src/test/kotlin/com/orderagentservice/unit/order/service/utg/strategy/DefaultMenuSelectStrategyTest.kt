package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.result.NodeCreationResult
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.menu.UiNodeIntegrator
import com.orderagentservice.order.service.utg.strategy.DefaultMenuSelectStrategy
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class DefaultMenuSelectStrategyTest {
    @MockK
    private lateinit var comparatorManager: ComparatorManager
    @MockK
    private lateinit var notificationService: NotificationService
    @MockK
    private lateinit var uiNodeIntegrator: UiNodeIntegrator
    @MockK
    private lateinit var screenNodeIntegrator: ScreenNodeIntegrator

    private lateinit var strategy: DefaultMenuSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = DefaultMenuSelectStrategy(
            comparatorManager,
            notificationService,
            uiNodeIntegrator,
            screenNodeIntegrator
        )
    }

    @Test
    @DisplayName("execute: 메뉴 노드 생성, 스크린 연결 및 클릭 명령 전송 성공")
    fun `execute should integrate menu node, link screen, and send click command`() {
        // given
        val kioskId = "kiosk-001"
        val menuTitle = "테스트_메뉴"
        val newNodeId = "node-menu-123"
        val categoryScreenId = "screen-cat-456"

        val context = mockk<UtgContext>()
        every { context.kioskId } returns kioskId

        val menuDto = mockk<MenuInfoDto>()
        every { menuDto.title } returns menuTitle

        val uiList = listOf(mockk<UiComponentDto>())
        val mockUiComponentParams = mockk<UiComponentParams>()

        val mockMatchDto = WordMatchDto(
            title = menuTitle,
            x = 150, y = 250,
            minX = 140, minY = 240, maxX = 160, maxY = 260,
            score = 1.0
        )
        val mockCreationResult = mockk<NodeCreationResult>()
        val mockCoordinateResponse = mockk<CoordinateDto>() // 요청대로 mock을 반환
        val coordinateSlot = slot<CoordinateDto>()

        every { comparatorManager.wordCompare(menuTitle, uiList) } returns mockMatchDto

        every { uiNodeIntegrator.integrateMenuNode(mockMatchDto, menuTitle, context) } returns mockCreationResult
        every { mockCreationResult.nodeId } returns newNodeId
        every { mockCreationResult.uiComponentParams } returns mockUiComponentParams

        every {
            screenNodeIntegrator.linkNode(
                kioskId = kioskId,
                nodeId = newNodeId,
                screenNodeId = categoryScreenId,
                uiComponentParams = mockUiComponentParams
            )
        } just runs

        every { notificationService.sendActionCommand(kioskId, capture(coordinateSlot)) } returns mockCoordinateResponse

        // when
        val result = strategy.execute(context, menuDto, uiList, categoryScreenId)

        // then
        assertEquals(newNodeId, result)

        verify(exactly = 1) { comparatorManager.wordCompare(menuTitle, uiList) }
        verify(exactly = 1) { uiNodeIntegrator.integrateMenuNode(mockMatchDto, menuTitle, context) }
        verify(exactly = 1) {
            screenNodeIntegrator.linkNode(
                kioskId = kioskId,
                nodeId = newNodeId,
                screenNodeId = categoryScreenId,
                uiComponentParams = mockUiComponentParams
            )
        }
        verify(exactly = 1) { notificationService.sendActionCommand(kioskId, any()) }

        val capturedCoordinate = coordinateSlot.captured
        assertEquals(mockMatchDto.x, capturedCoordinate.x)
        assertEquals(mockMatchDto.y, capturedCoordinate.y)
    }
}