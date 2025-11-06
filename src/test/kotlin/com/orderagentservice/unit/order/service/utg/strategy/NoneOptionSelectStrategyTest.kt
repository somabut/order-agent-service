package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.AllUiComponentDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.result.NodeCreationResult
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.menu.UiNodeIntegrator
import com.orderagentservice.order.service.utg.strategy.NoneOptionSelectStrategy
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
class NoneOptionSelectStrategyTest {
    @MockK
    private lateinit var comparatorManager: ComparatorManager
    @MockK
    private lateinit var screenNodeIntegrator: ScreenNodeIntegrator
    @MockK
    private lateinit var uiNodeIntegrator: UiNodeIntegrator
    @MockK
    private lateinit var uiDetectorManager: UiDetectorManager

    private lateinit var strategy: NoneOptionSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = NoneOptionSelectStrategy(
            comparatorManager,
            screenNodeIntegrator,
            uiNodeIntegrator,
            uiDetectorManager
        )
    }

    @Test
    @DisplayName("execute: uiDetector로 uiList를 새로고침한 후, 새 uiList로 옵션 노드 생성")
    fun `execute should refresh uiList via uiDetector and process options`() {
        // given
        val kioskId = "kiosk-001"
        val menuNodeId = "node-menu-123"
        val option1 = "옵션1"

        val context = mockk<UtgContext>()
        every { context.kioskId } returns kioskId
        every { context.screenNodeId } returns "screen-abc"

        val menuDto = mockk<MenuInfoDto>()
        every { menuDto.options } returns listOf(option1)

        val originalUiList = listOf(mockk<UiComponentDto>(relaxed = true))
        val updatedUiList = listOf(mockk<UiComponentDto>(relaxed = true))

        val mockUiDetectDto = mockk<AllUiComponentDto>()
        val mockUiComponentParams = mockk<UiComponentParams>()
        val mockMatchDto = mockk<WordMatchDto>()
        val mockCreationResult = mockk<NodeCreationResult>()

        every { uiDetectorManager.getUiComponents(context) } returns mockUiDetectDto
        every { mockUiDetectDto.uiElements } returns updatedUiList

        every { comparatorManager.wordCompare(option1, updatedUiList) } returns mockMatchDto

        every { uiNodeIntegrator.integrateOptionNode(mockMatchDto, option1, menuNodeId, context) } returns mockCreationResult
        every { mockCreationResult.nodeId } returns "node-opt-1"
        every { mockCreationResult.uiComponentParams } returns mockUiComponentParams

        every { screenNodeIntegrator.linkNode(kioskId, "node-opt-1", "screen-abc", mockUiComponentParams) } just runs

        // when
        val result = strategy.execute(context, menuDto, menuNodeId, originalUiList)

        // then
        assertEquals(updatedUiList, result)

        verify(exactly = 1) { uiDetectorManager.getUiComponents(context) }

        verify(exactly = 1) { comparatorManager.wordCompare(option1, updatedUiList) }
        verify(exactly = 0) { comparatorManager.wordCompare(option1, originalUiList) }

        verify(exactly = 1) { uiNodeIntegrator.integrateOptionNode(mockMatchDto, option1, menuNodeId, context) }
        verify(exactly = 1) { screenNodeIntegrator.linkNode(kioskId, "node-opt-1", "screen-abc", mockUiComponentParams) }
    }
}