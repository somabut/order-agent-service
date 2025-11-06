package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.result.NodeCreationResult
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.menu.UiNodeIntegrator
import com.orderagentservice.order.service.utg.strategy.DefaultOptionSelectStrategy
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class DefaultOptionSelectStrategyTest {
    @MockK
    private lateinit var comparatorManager: ComparatorManager
    @MockK
    private lateinit var screenNodeIntegrator: ScreenNodeIntegrator
    @MockK
    private lateinit var uiNodeIntegrator: UiNodeIntegrator

    private lateinit var strategy: DefaultOptionSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = DefaultOptionSelectStrategy(
            comparatorManager,
            screenNodeIntegrator,
            uiNodeIntegrator
        )
    }

    @Test
    @DisplayName("execute: 모든 옵션에 대해 노드 생성 및 스크린 연결 실행")
    fun `execute should process all options and return original uiList`() {
        // given
        val kioskId = "kiosk-001"
        val menuNodeId = "node-menu-123"
        val option1 = "옵션1"
        val option2 = "옵션2"

        val context = mockk<UtgContext>()
        every { context.kioskId } returns kioskId
        every { context.screenNodeId } returns "screen-abc"

        val menuDto = mockk<MenuInfoDto>()
        every { menuDto.options } returns listOf(option1, option2)

        val uiList = listOf(mockk<UiComponentDto>()) // 원본 uiList
        val mockUiComponentParams = mockk<UiComponentParams>()
        val mockMatchDto1 = mockk<WordMatchDto>()
        val mockMatchDto2 = mockk<WordMatchDto>()
        val mockCreationResult1 = mockk<NodeCreationResult>()
        val mockCreationResult2 = mockk<NodeCreationResult>()

        every { comparatorManager.wordCompare(option1, uiList) } returns mockMatchDto1
        every { comparatorManager.wordCompare(option2, uiList) } returns mockMatchDto2

        every { uiNodeIntegrator.integrateOptionNode(mockMatchDto1, option1, menuNodeId, context) } returns mockCreationResult1
        every { mockCreationResult1.nodeId } returns "node-opt-1"
        every { mockCreationResult1.uiComponentParams } returns mockUiComponentParams

        every { uiNodeIntegrator.integrateOptionNode(mockMatchDto2, option2, menuNodeId, context) } returns mockCreationResult2
        every { mockCreationResult2.nodeId } returns "node-opt-2"
        every { mockCreationResult2.uiComponentParams } returns mockUiComponentParams

        every { screenNodeIntegrator.linkNode(kioskId, "node-opt-1", "screen-abc", mockUiComponentParams) } just runs
        every { screenNodeIntegrator.linkNode(kioskId, "node-opt-2", "screen-abc", mockUiComponentParams) } just runs

        // when
        val result = strategy.execute(context, menuDto, menuNodeId, uiList)

        // then
        assertEquals(uiList, result)

        verify(exactly = 1) { comparatorManager.wordCompare(option1, uiList) }
        verify(exactly = 1) { comparatorManager.wordCompare(option2, uiList) }

        verify(exactly = 1) { uiNodeIntegrator.integrateOptionNode(mockMatchDto1, option1, menuNodeId, context) }
        verify(exactly = 1) { uiNodeIntegrator.integrateOptionNode(mockMatchDto2, option2, menuNodeId, context) }

        verify(exactly = 1) { screenNodeIntegrator.linkNode(kioskId, "node-opt-1", "screen-abc", mockUiComponentParams) }
        verify(exactly = 1) { screenNodeIntegrator.linkNode(kioskId, "node-opt-2", "screen-abc", mockUiComponentParams) }
    }
}