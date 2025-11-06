package com.orderagentservice.unit.order.service.utg.orchestrator

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.AllUiComponentDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.auto.AutoTaskExecutor
import com.orderagentservice.order.service.graph.info.InfoGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.UtgActionFactory
import com.orderagentservice.order.service.utg.orchestrator.UtgInitializeOrchestrator
import com.orderagentservice.order.service.utg.orchestrator.UtgUpdateOrchestrator
import com.orderagentservice.order.service.utg.strategy.BackSelectStrategy
import com.orderagentservice.order.service.utg.strategy.OptionSelectStrategy
import io.mockk.Awaits
import io.mockk.Runs
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
import org.mockito.Mock

@ExtendWith(MockKExtension::class)
class UtgUpdateOrchestratorTest {
    @MockK
    private lateinit var autoTaskExecutor: AutoTaskExecutor
    @MockK
    private lateinit var utgInitializeOrchestrator: UtgInitializeOrchestrator
    @MockK
    private lateinit var utgActionFactory: UtgActionFactory
    @MockK
    private lateinit var uiGraphService: UiGraphService
    @MockK
    private lateinit var infoGraphService: InfoGraphService
    @MockK
    private lateinit var uiDetectorManager: UiDetectorManager

    private lateinit var orchestrator: UtgUpdateOrchestrator

    private val MOCK_KIOSK_ID = "k-123"
    private val MOCK_ROOT_NODE_ID = "root-id"
    private val MOCK_STATION_NODE_ID = "station-id"
    private val MOCK_CATEGORY_NODE_ID = "category-id"

    @MockK
    private lateinit var mockContext: UtgContext
    @MockK
    private lateinit var mockRootNode: ActionPathDto
    @MockK
    private lateinit var mockStationNode: ActionPathDto
    @MockK
    private lateinit var mockStrategyRequest: UtgStrategyRequest
    @MockK
    private lateinit var mockActionProfile: UtgActionProfile
    @MockK
    private lateinit var mockOptionStrategy: OptionSelectStrategy
    @MockK
    private lateinit var mockBackStrategy: BackSelectStrategy
    @MockK
    private lateinit var mockUiComponentsResult: AllUiComponentDto
    @MockK
    private lateinit var mockUiList: List<UiComponentDto>

    @BeforeEach
    fun setUp() {
        orchestrator = UtgUpdateOrchestrator(
            autoTaskExecutor,
            utgInitializeOrchestrator,
            utgActionFactory,
            mockk(),
            uiGraphService,
            infoGraphService,
            uiDetectorManager
        )

        every { mockContext.kioskId } returns MOCK_KIOSK_ID
        every { mockContext.lastNodeId } returns MOCK_CATEGORY_NODE_ID
        every { uiGraphService.findRoot(MOCK_KIOSK_ID) } returns mockRootNode
        every { mockRootNode.id } returns MOCK_ROOT_NODE_ID
        every { uiGraphService.findStation(MOCK_KIOSK_ID) } returns mockStationNode
        every { mockStationNode.id } returns MOCK_STATION_NODE_ID
        every { autoTaskExecutor.clickPlace(any()) } returns true
        every { infoGraphService.findLinkedInfo(MOCK_KIOSK_ID) } returns mockStrategyRequest
        every { utgActionFactory.createProfile(mockStrategyRequest) } returns mockActionProfile

        every { utgInitializeOrchestrator.navigateMenus(any(), any(), any()) } just runs
        every { utgInitializeOrchestrator.navigatePayment(any(), any()) } just runs
        every { uiGraphService.changeModified(any(), any()) } just runs

        every { mockActionProfile.optionSelectStrategy } returns mockOptionStrategy
        every { mockActionProfile.backSelectStrategy } returns mockBackStrategy

        every { uiDetectorManager.getUiComponents(mockContext) } returns mockUiComponentsResult
        every { mockUiComponentsResult.uiElements } returns mockUiList

        every { mockContext.stationNodeId = any() } just runs
        every { mockContext.currentCategory = any() } just runs
        every { mockContext.lastNodeId = any() } just runs
    }

    @Test
    @DisplayName("editCategories: 카테고리 1개, 남은 메뉴 없음, 결제 초기화 O")
    fun `editCategories should navigate category, find no remain, and init payment`() {
        // given
        val modifiedCategoryList = listOf("Coffee")
        val pendingList = listOf(MenuInfoDto("Coffee", listOf("Americano"), "Coffee"))
        val menuList = listOf(MenuInfoDto("Coffee", listOf("Americano"), "Coffee"))
        val isInitPayment = true

        every { autoTaskExecutor.clickCategory(any(), "Coffee") } returns MOCK_CATEGORY_NODE_ID

        val completedNode = mockk<UiDto>()
        every { completedNode.type } returns NodeType.MENU
        every { completedNode.title } returns "Americano"
        every { uiGraphService.findAll(MOCK_KIOSK_ID) } returns listOf(completedNode)

        // when
        orchestrator.editCategories(mockContext, modifiedCategoryList, pendingList, menuList, isInitPayment)

        // then
        verify(exactly = 1) { uiGraphService.findRoot(MOCK_KIOSK_ID) }
        verify(exactly = 1) { autoTaskExecutor.clickPlace(any()) }
        verify(exactly = 1) { utgActionFactory.createProfile(mockStrategyRequest) }

        verify(exactly = 1) { autoTaskExecutor.clickCategory(any(), "Coffee") }
        verify(exactly = 2) { utgInitializeOrchestrator.navigateMenus(any(), any(), any()) }

        verify(exactly = 1) { uiGraphService.findAll(MOCK_KIOSK_ID) }
        verify(exactly = 1) { uiGraphService.changeModified(MOCK_KIOSK_ID, "Coffee") }

        verify(exactly = 1) { utgInitializeOrchestrator.navigatePayment(mockContext, mockActionProfile) }
    }

    @Test
    @DisplayName("editCategories: 카테고리 0개, 남은 메뉴 있음, 결제 초기화 X")
    fun `editCategories should skip loop, find remain, and skip payment`() {
        // given
        val modifiedCategoryList = emptyList<String>()
        val pendingList = emptyList<MenuInfoDto>()
        val menuList = listOf(MenuInfoDto("Coffee", listOf("Americano"), "beverage"))
        val isInitPayment = false

        every { uiGraphService.findAll(MOCK_KIOSK_ID) } returns emptyList()

        // when
        orchestrator.editCategories(mockContext, modifiedCategoryList, pendingList, menuList, isInitPayment)

        // then
        verify(exactly = 1) { uiGraphService.findRoot(MOCK_KIOSK_ID) }
        verify(exactly = 1) { utgActionFactory.createProfile(mockStrategyRequest) }

        verify(exactly = 1) { uiGraphService.findAll(MOCK_KIOSK_ID) }
        verify(exactly = 1) { utgInitializeOrchestrator.navigateMenus(mockContext, menuList, mockActionProfile) }

        verify(exactly = 0) { autoTaskExecutor.clickCategory(any(), any()) }
        verify(exactly = 0) { uiGraphService.changeModified(any(), any()) }
        verify(exactly = 0) { utgInitializeOrchestrator.navigatePayment(any(), any()) }
    }

    @Test
    @DisplayName("editMenus: 메뉴 1개 (옵션 X), 남은 메뉴 없음, 결제 X")
    fun `editMenus should navigate menu without options and skip payment`() {
        // given
        val menuDto = MenuInfoDto("Coffee", listOf(), "Coffee")
        val pendingList = listOf(menuDto)
        val menuList = listOf(menuDto)
        val modifiedMenuList = listOf("Americano")
        val isInitPayment = false

        val mockAutoMenu = menuDto.toAutoOrderMenu()
        val mockClickedMenuNode = mockk<ActionPathDto>()

        every { mockClickedMenuNode.id } returns "menu-node-id"
        every { autoTaskExecutor.clickMenu(any(), mockAutoMenu) } returns mockClickedMenuNode
        every { uiGraphService.findNodeByTitle(MOCK_KIOSK_ID, "Coffee") } returns MOCK_CATEGORY_NODE_ID
        every { uiGraphService.findRoot(any()) } returns mockRootNode

        every { mockBackStrategy.execute(mockContext, "menu-node-id", mockUiList, false) } returns "back-node-id"
        every { uiGraphService.saveRel("back-node-id", MOCK_CATEGORY_NODE_ID, NodeRelationType.BACK_TO) } just runs

        val completedNode = mockk<UiDto>()
        every { completedNode.type } returns NodeType.MENU
        every { completedNode.title } returns "Americano"
        every { uiGraphService.findAll(MOCK_KIOSK_ID) } returns listOf(completedNode)

        // when
        orchestrator.editMenus(mockContext, pendingList, menuList, modifiedMenuList, isInitPayment)

        // then
        verify(exactly = 0) { mockOptionStrategy.execute(any(), any(), any(), any()) }
        verify(exactly = 0) { utgInitializeOrchestrator.navigatePayment(any(), any()) }
        verify(exactly = 1) { mockBackStrategy.execute(mockContext, "menu-node-id", mockUiList, false) }
    }

    @Test
    @DisplayName("editPayment: 수정된 노드까지 이동 후 결제 끝까지 진행")
    fun `editPayment should navigate to ui and complete payment`() {
        // given
        val nowUi = "Card"
        val randomMenu = MenuInfoDto("Coffee", listOf("Americano"), "Coffee")
        val mockAutoMenu = randomMenu.toAutoOrderMenu()

        val mockAction1 = mockk<ActionPathDto>()
        val mockAction2 = mockk<ActionPathDto>()
        val actionList = listOf(mockAction1, mockAction2)

        every { mockAction1.title } returns "Action1"
        every { mockAction2.id } returns "action-2-id"
        every { mockAction2.title } returns "Card"

        every { uiGraphService.findPath(MOCK_KIOSK_ID, MOCK_ROOT_NODE_ID, nowUi) } returns actionList
        every { autoTaskExecutor.clickMenu(any(), mockAutoMenu) } returns mockk()
        every { autoTaskExecutor.clickPayment(any(), mockAction1) } just runs
        every { autoTaskExecutor.clickPayment(any(), mockAction2) } just runs

        // when
        orchestrator.editPayment(mockContext, nowUi, randomMenu)

        // then
        verify(exactly = 1) { uiGraphService.findRoot(MOCK_KIOSK_ID) }
        verify(exactly = 1) { utgActionFactory.createProfile(mockStrategyRequest) }
        verify(exactly = 1) { uiGraphService.findPath(MOCK_KIOSK_ID, MOCK_ROOT_NODE_ID, nowUi) }
        verify(exactly = 1) { autoTaskExecutor.clickMenu(any(), mockAutoMenu) }
        verify(exactly = 1) { autoTaskExecutor.clickPayment(any(), mockAction1) }
        verify(exactly = 1) { autoTaskExecutor.clickPayment(any(), mockAction2) }
        verify(exactly = 1) { mockContext.lastNodeId = "action-2-id" }
        verify(exactly = 1) { utgInitializeOrchestrator.navigatePayment(mockContext, mockActionProfile) }
    }
}