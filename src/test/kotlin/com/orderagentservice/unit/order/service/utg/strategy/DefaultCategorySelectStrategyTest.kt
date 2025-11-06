package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.result.NodeCreationResult
import com.orderagentservice.order.model.type.LogicType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.menu.UiNodeIntegrator
import com.orderagentservice.order.service.utg.strategy.DefaultCategorySelectStrategy
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class DefaultCategorySelectStrategyTest {
    @MockK
    private lateinit var comparatorManager: ComparatorManager
    @MockK
    private lateinit var notificationService: NotificationService
    @MockK
    private lateinit var uiNodeIntegrator: UiNodeIntegrator

    private lateinit var strategy: DefaultCategorySelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = DefaultCategorySelectStrategy(
            comparatorManager,
            notificationService,
            uiNodeIntegrator
        )
    }

    @Test
    @DisplayName("execute: 카테고리 노드 생성 및 클릭 명령 전송 성공")
    fun `execute should integrate category node and send click command`() {
        // given
        val kioskId = "kiosk-001"
        val categoryName = "테스트_카테고리"
        val newNodeId = "node-cat-123"

        val context = spyk(
            UtgContext(
                kioskId = kioskId, screenNodeId = "screen-001",
                menuBackUi = null, logicState = LogicType.INITIALIZE, isPlaceDetermined = true,
                imageName = "imageName", history = mutableListOf(), pushedImages = mutableListOf(),
                lastNodeId = "old-node-id", // <-- 이전 노드 ID
                stationNodeId = "", currentCategory = ""
            )
        )

        val menuDto = mockk<MenuInfoDto>()
        every { menuDto.category } returns categoryName

        val uiList = listOf(mockk<UiComponentDto>())

        val mockMatchDto = WordMatchDto(
            title = categoryName,
            x = 100, y = 200,
            minX = 90, minY = 190, maxX = 110, maxY = 210,
            score = 1.0
        )
        val mockCreationResult = mockk<NodeCreationResult>()
        val mockCoordinateResponse = mockk<CoordinateDto>() // 요청대로 mock을 반환
        val coordinateSlot = slot<CoordinateDto>()

        every { comparatorManager.wordCompare(categoryName, uiList) } returns mockMatchDto
        every { uiNodeIntegrator.integrateCategoryNode(mockMatchDto, categoryName, context) } returns mockCreationResult
        every { mockCreationResult.nodeId } returns newNodeId
        every { notificationService.sendActionCommand(kioskId, capture(coordinateSlot)) } returns mockCoordinateResponse

        // when
        val result = strategy.execute(context, menuDto, uiList)

        // then
        assertEquals(mockCreationResult, result)

        assertEquals(newNodeId, context.lastNodeId)

        verify(exactly = 1) { comparatorManager.wordCompare(categoryName, uiList) }
        verify(exactly = 1) { uiNodeIntegrator.integrateCategoryNode(mockMatchDto, categoryName, context) }

        val capturedCoordinate = coordinateSlot.captured
        assertEquals(mockMatchDto.x, capturedCoordinate.x)
        assertEquals(mockMatchDto.y, capturedCoordinate.y)
    }
}