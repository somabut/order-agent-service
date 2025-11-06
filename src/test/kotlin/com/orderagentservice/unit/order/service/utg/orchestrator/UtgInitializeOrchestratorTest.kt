package com.orderagentservice.unit.order.service.utg.orchestrator

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.exception.UtgInfiniteLoopException
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.AllUiComponentDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.result.CategorySequenceResult
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.UtgActionFactory
import com.orderagentservice.order.service.utg.orchestrator.UtgInitializeOrchestrator
import com.orderagentservice.order.service.utg.sequencer.CategoryActionSequencer
import com.orderagentservice.order.service.utg.sequencer.MenuActionSequencer
import com.orderagentservice.order.service.utg.sequencer.PaymentActionSequencer
import com.orderagentservice.order.service.utg.strategy.StartSelectStrategy
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UtgInitializeOrchestratorTest {
    @MockK
    private lateinit var utgActionFactory: UtgActionFactory
    @MockK
    private lateinit var categoryActionSequencer: CategoryActionSequencer
    @MockK
    private lateinit var menuActionSequencer: MenuActionSequencer
    @MockK
    private lateinit var paymentActionSequencer: PaymentActionSequencer
    @MockK
    private lateinit var uiDetectorManager: UiDetectorManager
    @MockK(relaxed = true)
    private lateinit var logService: LogService

    private lateinit var orchestrator: UtgInitializeOrchestrator

    @MockK private lateinit var mockContext: UtgContext
    @MockK private lateinit var mockMenuList: List<MenuInfoDto>
    @MockK private lateinit var mockStrategyRequest: UtgStrategyRequest
    @MockK private lateinit var mockActionProfile: UtgActionProfile
    @MockK private lateinit var mockStartStrategy: StartSelectStrategy
    @MockK private lateinit var mockUiComponentsResult: AllUiComponentDto
    @MockK private lateinit var mockUiList: List<UiComponentDto>
    @MockK private lateinit var mockCategoryResult: CategorySequenceResult

    private val MOCK_KIOSK_ID = "k-123"
    private val MOCK_STATION_NODE_ID = "station-001"
    private val MOCK_START_SCREEN_ID = "screen-start"
    private val MOCK_CATEGORY_SCREEN_ID = "screen-category"

    @BeforeEach
    fun setUp() {
        orchestrator = UtgInitializeOrchestrator(
            utgActionFactory,
            categoryActionSequencer,
            menuActionSequencer,
            paymentActionSequencer,
            uiDetectorManager,
            logService
        )

        every { mockContext.kioskId } returns MOCK_KIOSK_ID
        every { mockContext.stationNodeId } returns MOCK_STATION_NODE_ID
        every { mockContext.screenNodeId } returns MOCK_START_SCREEN_ID

        every { mockContext.lastNodeId = any() } just runs

        every { utgActionFactory.createProfile(mockStrategyRequest) } returns mockActionProfile
        every { mockActionProfile.startSelectStrategy } returns mockStartStrategy
        every { mockStartStrategy.execute(mockContext, mockStrategyRequest) } just runs
    }

    @Test
    @DisplayName("execute: 전체 오케스트레이션이 순서대로 실행된다")
    fun `execute should run all steps in order`() {
        // given
        val menuDto = MenuInfoDto("커피", listOf("아메리카노"), category = "category")
        val menuList = listOf(menuDto)

        every { uiDetectorManager.getUiComponents(mockContext) } returns mockUiComponentsResult
        every { mockUiComponentsResult.uiElements } returns mockUiList
        every { categoryActionSequencer.run(any(), any(), any(), any(), any()) } returns mockCategoryResult
        every { mockCategoryResult.uiList } returns mockUiList
        every { mockCategoryResult.categoryScreenId } returns MOCK_CATEGORY_SCREEN_ID
        every { menuActionSequencer.run(any(), any(), any(), any(), any()) } just runs

        every { paymentActionSequencer.run(mockContext, mockActionProfile) } returns true

        // when
        orchestrator.execute(mockContext, menuList, mockStrategyRequest)

        //then
        verify(exactly = 1) { utgActionFactory.createProfile(mockStrategyRequest) }
        verify(exactly = 1) { mockStartStrategy.execute(mockContext, mockStrategyRequest) }
        verify(exactly = 1) { uiDetectorManager.getUiComponents(mockContext) }
        verify(exactly = 1) { categoryActionSequencer.run(mockContext, menuDto, mockActionProfile, mockUiList, MOCK_START_SCREEN_ID) }
        verify(exactly = 1) { logService.printLog(any<Any>()) }
        verify(exactly = 1) { menuActionSequencer.run(mockContext, menuDto, mockActionProfile, mockUiList, MOCK_CATEGORY_SCREEN_ID) }
        verify(exactly = 1) { paymentActionSequencer.run(mockContext, mockActionProfile) }
    }

    @Test
    @DisplayName("navigateMenus: 메뉴 리스트가 비어있으면 시퀀서들이 호출되지 않는다")
    fun `MapsMenus should not call sequencers if menu list is empty`() {
        // given
        val emptyMenuList = emptyList<MenuInfoDto>()

        every { uiDetectorManager.getUiComponents(mockContext) } returns mockUiComponentsResult
        every { mockUiComponentsResult.uiElements } returns mockUiList

        // when
        orchestrator.navigateMenus(mockContext, emptyMenuList, mockActionProfile)

        // then
        verify(exactly = 1) { uiDetectorManager.getUiComponents(mockContext) }

        verify(exactly = 0) { categoryActionSequencer.run(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { logService.printLog(any<Any>()) }
        verify(exactly = 0) { menuActionSequencer.run(any(), any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("navigateMenus: 메뉴 리스트가 여러 개일 때, UI 상태가 다음 루프로 전파된다")
    fun `MapsMenus should propagate ui state across loop iterations`() {
        // given
        val menuDto1 = MenuInfoDto("커피", listOf("아메리카노"), category = "음료")
        val menuDto2 = MenuInfoDto("디저트", listOf("케이크"), category = "음료")
        val menuList = listOf(menuDto1, menuDto2)

        val mockUiList1 = mockk<List<UiComponentDto>>()
        val mockCategoryResult1 = mockk<CategorySequenceResult>()
        val mockUiList2 = mockk<List<UiComponentDto>>()
        val screenId1 = "screen-1"

        val mockCategoryResult2 = mockk<CategorySequenceResult>()
        val mockUiList3 = mockk<List<UiComponentDto>>()
        val screenId2 = "screen-2"

        every { uiDetectorManager.getUiComponents(mockContext) } returns mockUiComponentsResult
        every { mockUiComponentsResult.uiElements } returns mockUiList1

        every { categoryActionSequencer.run(mockContext, menuDto1, mockActionProfile, mockUiList1, MOCK_START_SCREEN_ID) } returns mockCategoryResult1
        every { mockCategoryResult1.uiList } returns mockUiList2
        every { mockCategoryResult1.categoryScreenId } returns screenId1
        every { menuActionSequencer.run(mockContext, menuDto1, mockActionProfile, mockUiList2, screenId1) } just runs

        every { categoryActionSequencer.run(mockContext, menuDto2, mockActionProfile, mockUiList2, screenId1) } returns mockCategoryResult2
        every { mockCategoryResult2.uiList } returns mockUiList3
        every { mockCategoryResult2.categoryScreenId } returns screenId2
        every { menuActionSequencer.run(mockContext, menuDto2, mockActionProfile, mockUiList3, screenId2) } just runs

        // when
        orchestrator.navigateMenus(mockContext, menuList, mockActionProfile)

        // then
        verify { categoryActionSequencer.run(mockContext, menuDto1, mockActionProfile, mockUiList1, MOCK_START_SCREEN_ID) }
        verify { menuActionSequencer.run(mockContext, menuDto1, mockActionProfile, mockUiList2, screenId1) }

        verify { categoryActionSequencer.run(mockContext, menuDto2, mockActionProfile, mockUiList2, screenId1) }
        verify { menuActionSequencer.run(mockContext, menuDto2, mockActionProfile, mockUiList3, screenId2) }
    }

    @Test
    @DisplayName("navigatePayment: 시퀀서가 true를 반환하면 정상 종료된다")
    fun `MapsPayment should complete normally if sequencer returns true`() {
        // given
        every { paymentActionSequencer.run(mockContext, mockActionProfile) } returns true

        // when
        orchestrator.navigatePayment(mockContext, mockActionProfile)

        // then
        verify(exactly = 1) { paymentActionSequencer.run(mockContext, mockActionProfile) }
    }

    @Test
    @DisplayName("navigatePayment: 시퀀서가 false를 반환하면 UtgInfiniteLoopException을 던진다")
    fun `MapsPayment should throw exception if sequencer returns false`() {
        // given
        every { paymentActionSequencer.run(mockContext, mockActionProfile) } returns false

        // when & then
        assertThrows<UtgInfiniteLoopException> {
            orchestrator.navigatePayment(mockContext, mockActionProfile)
        }

        verify(exactly = 1) { paymentActionSequencer.run(mockContext, mockActionProfile) }
    }
}