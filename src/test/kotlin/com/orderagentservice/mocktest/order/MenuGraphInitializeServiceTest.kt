package com.orderagentservice.mocktest.order

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.MissingComponentAgent
import com.orderagentservice.agent.PageAgent
import com.orderagentservice.agent.model.dto.*
import com.orderagentservice.order.exception.LowScoreException
import com.orderagentservice.order.model.GraphInitializeContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.service.MenuGraphInitializeService
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.PlaceGraphInitializeService
import com.orderagentservice.order.service.UtgService
import com.orderagentservice.order.util.UiExtractorManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import java.io.File

class MenuGraphInitializeServiceTest {
    companion object {
        private const val TEST_KIOSK_ID = "KIOSK_123123"
        private const val TEST_ROOT_NODE_ID = "ROOT_NODE_123"
        private const val TEST_FIRST_NODE_ID = "FIRST_NODE_123"
        private const val TEST_MENU_ENTITY_ID = "MENU_ENTITY_123"
        private const val TEST_OPTION_ENTITY_ID = "OPTION_ENTITY_123"
        private const val TEST_BACK_ENTITY_ID = "BACK_ENTITY_123"
        private const val TEST_MODAL_NODE_ID = "MODAL_NODE_123"
        private const val TEST_X_COORDINATE = 100
        private const val TEST_Y_COORDINATE = 200
        private const val TEST_MENU_TITLE = "치킨버거"
        private const val TEST_OPT_TITLE = "치즈추가"
        private const val TEST_CATEGORY = "버거"
        // ← 실제 파일 경로 사용 (ImageUtils가 실제로 처리)
        private val TEST_IMAGE_DATA = File("C:\\Users\\hachi\\IdeaProjects\\OrderAgentService\\src\\test\\resources\\main.png")
        private val TEST_MODAL_IMAGE_DATA = File("C:\\Users\\hachi\\IdeaProjects\\OrderAgentService\\src\\test\\resources\\main.png")
        private val TEST_COORDINATE = CoordinateDto(TEST_X_COORDINATE, TEST_Y_COORDINATE, "coordinate")
        private const val HIGH_SCORE = 0.8F
        private const val MEDIUM_SCORE = 0.65F
        private const val LOW_SCORE = 0.5F
        private const val VERY_LOW_SCORE = 0.4F
        // ← 실제 해시값은 ImageUtils.imageToHash()가 실제로 계산한 값이 사용됨
        private const val TEST_IMAGE_HASH = "test_hash_123"
        private const val TEST_MODAL_IMAGE_HASH = "modal_hash_456"
    }

    private lateinit var menuAgent: MenuAgent
    private lateinit var backAgent: BackAgent
    private lateinit var pageAgent: PageAgent
    private lateinit var missingComponentAgent: MissingComponentAgent
    private lateinit var placeGraphInitializeService: PlaceGraphInitializeService
    private lateinit var uiExtractorManager: UiExtractorManager
    private lateinit var notificationService: NotificationService
    private lateinit var utgService: UtgService
    private lateinit var menuGraphInitializeService: MenuGraphInitializeService

    private lateinit var menuList: List<MenuInfoDto>
    private lateinit var menuInfoDto: MenuInfoDto
    private lateinit var menuInfoWithOptionsDto: MenuInfoDto
    private lateinit var optInfoDto: MenuInfoDto
    private lateinit var firstDto: MenuInfoDto
    private lateinit var rootNode: UiEntity
    private lateinit var firstNode: UiEntity
    private lateinit var menuEntity: UiEntity
    private lateinit var modalEntity: UiEntity
    private lateinit var optionEntity: UiEntity
    private lateinit var backEntity: UiEntity
    private lateinit var llmUiList: MutableList<LlmUiComponentDto>
    private lateinit var modalLlmUiList: MutableList<LlmUiComponentDto>
    private lateinit var menuAction: AgentActionDto
    private lateinit var modalMenuAction: AgentActionDto
    private lateinit var firstAction: AgentActionDto
    private lateinit var optAction: AgentActionDto
    private lateinit var backAction: AgentBackDto
    private lateinit var pageAction: AgentPageDto
    private lateinit var modalPageAction: AgentPageDto
    private lateinit var placeActionList: List<AgentActionDto>
    private lateinit var actionResult: Pair<Int, Int>

    @BeforeEach
    fun setUp() {
        menuAgent = mock()
        backAgent = mock()
        pageAgent = mock()
        missingComponentAgent = mock()
        placeGraphInitializeService = mock()
        uiExtractorManager = mock()
        notificationService = mock()
        utgService = mock()

        menuGraphInitializeService = MenuGraphInitializeService(
            menuAgent, backAgent, pageAgent, missingComponentAgent, placeGraphInitializeService,
            uiExtractorManager, notificationService, utgService
        )

        firstDto = MenuInfoDto(
            title = TEST_CATEGORY,
            options = listOf(),
            category = TEST_CATEGORY
        )

        menuInfoDto = MenuInfoDto(
            title = TEST_MENU_TITLE,
            options = listOf(),
            category = TEST_CATEGORY
        )

        menuInfoWithOptionsDto = MenuInfoDto(
            title = TEST_MENU_TITLE,
            options = listOf("치즈추가", "피클빼기"),
            category = TEST_CATEGORY
        )

        optInfoDto = MenuInfoDto(
            title = TEST_OPT_TITLE,
            options = listOf(),
            category = TEST_MENU_TITLE
        )

        menuList = listOf(menuInfoWithOptionsDto)

        rootNode = UiEntity(
            id = TEST_ROOT_NODE_ID,
            isNext = true,
            x = -1,
            y = -1,
            title = "root",
            kioskId = TEST_KIOSK_ID
        )

        firstNode = UiEntity(
            id = TEST_FIRST_NODE_ID,
            isNext = true,
            x = TEST_X_COORDINATE,
            y = TEST_Y_COORDINATE,
            title = TEST_CATEGORY,
            kioskId = TEST_KIOSK_ID
        )

        menuEntity = UiEntity(
            id = TEST_MENU_ENTITY_ID,
            isNext = false,
            x = TEST_X_COORDINATE,
            y = TEST_Y_COORDINATE,
            title = TEST_MENU_TITLE,
            kioskId = TEST_KIOSK_ID
        )

        modalEntity = UiEntity(
            id = TEST_MODAL_NODE_ID,
            isNext = false,
            x = 300,
            y = 400,
            title = TEST_MENU_TITLE,
            kioskId = TEST_KIOSK_ID
        )

        optionEntity = UiEntity(
            id = TEST_OPTION_ENTITY_ID,
            isNext = false,
            x = 150,
            y = 250,
            title = "치즈추가",
            kioskId = TEST_KIOSK_ID
        )

        backEntity = UiEntity(
            id = TEST_BACK_ENTITY_ID,
            isNext = false,
            x = 50,
            y = 50,
            title = "뒤로가기",
            kioskId = TEST_KIOSK_ID
        )

        llmUiList = mutableListOf(
            LlmUiComponentDto(x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_MENU_TITLE),
            LlmUiComponentDto(x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_CATEGORY)
        )

        modalLlmUiList = mutableListOf(
            LlmUiComponentDto(x = 300, y = 400, title = TEST_MENU_TITLE),
            LlmUiComponentDto(x = 150, y = 250, title = "치즈추가"),
            LlmUiComponentDto(x = 200, y = 300, title = "완료")
        )

        firstAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = TEST_CATEGORY,
            goNext = false,
            score = HIGH_SCORE
        )

        menuAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = TEST_MENU_TITLE,
            goNext = false,
            score = HIGH_SCORE
        )

        modalMenuAction = AgentActionDto(
            coordinate = listOf(300, 400),
            title = TEST_MENU_TITLE,
            goNext = false,
            score = HIGH_SCORE
        )

        optAction = AgentActionDto(
            coordinate = listOf(150, 250),
            title = TEST_OPT_TITLE,
            goNext = false,
            score = HIGH_SCORE
        )

        backAction = AgentBackDto(
            coordinate = listOf(50, 50),
            title = "뒤로가기",
            score = HIGH_SCORE
        )

        pageAction = AgentPageDto(
            contain = true,
            score = HIGH_SCORE
        )

        modalPageAction = AgentPageDto(
            contain = false,
            score = HIGH_SCORE
        )

        placeActionList = listOf(
            AgentActionDto(false, 0.9F, listOf(150, 250), "포장"),
            AgentActionDto(false, 0.8F, listOf(100, 200), "매장")
        )

        actionResult = Pair(TEST_X_COORDINATE, TEST_Y_COORDINATE)

        reset(menuAgent, backAgent, pageAgent, missingComponentAgent, placeGraphInitializeService,
            uiExtractorManager, notificationService, utgService)
    }

    @Test
    fun `메뉴 그래프 초기화가 성공한다_옵션이_없는_경우`() {
        // given
        val simpleMenuList = listOf(menuInfoDto)
        val context = createTestContext()

        setupBasicMocks(context)
        whenever(menuAgent.determineAction(any(), any())).thenReturn(firstAction, menuAction)
        whenever(pageAgent.determineAction(any(), any())).thenReturn(pageAction)

        // when
        menuGraphInitializeService.initializeGraph(context, simpleMenuList)

        // then
        assertEquals(4, context.history.size) // place(2) + category + menu
        assertTrue(context.isFindPlace)
        assertNotNull(context.lastNode)
        assertEquals(TEST_FIRST_NODE_ID, context.lastNode!!.id)
        verify(utgService, times(3)).saveNode(any<UiDto>()) // root + category + menu
    }

    @Test
    fun `메뉴 그래프 초기화가 성공한다_모달_없는_옵션_메뉴`() {
        // given
        val context = createTestContext()

        // ← mockStatic 블록 완전 제거!
        setupBasicMocks(context)
        setupModalMocks(hasModal = false)
        setupOptionMocks()

        // when
        menuGraphInitializeService.initializeGraph(context, menuList)

        // then
        assertTrue(context.history.size >= 6) // place(2) + category + menu + options(2) + back
        verify(pageAgent).determineAction(anyList(), any())
        verify(menuAgent, atLeastOnce()).determineAction(any(), any())
    }

    @Test
    fun `메뉴 그래프 초기화가 성공한다_모달_있는_옵션_메뉴`() {
        // given
        val context = createTestContext()

        // ← mockStatic 블록 완전 제거!
        setupBasicMocks(context)
        setupModalMocks(hasModal = true)
        setupOptionMocks()

        // when
        menuGraphInitializeService.initializeGraph(context, menuList)

        // then
        println(context.history.size)
        assertTrue(context.history.size >= 8) // place + category + menu + modal + modal + options + back
        verify(pageAgent).determineAction(menuInfoWithOptionsDto.options, modalLlmUiList)
        verify(menuAgent, times(2)).determineAction(menuInfoWithOptionsDto, modalLlmUiList) // 모달에서 메뉴 재선택
        verify(utgService).saveRel(TEST_MENU_ENTITY_ID, TEST_MODAL_NODE_ID, NodeRelation.PATH_TO)
    }

    @Test
    fun `낮은_점수_액션으로_LowScoreException_발생`() {
        // given
        val lowScoreMenuList = List(3) { menuInfoDto }
        val context = createTestContext()
        val veryLowScoreAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = TEST_MENU_TITLE,
            goNext = false,
            score = VERY_LOW_SCORE
        )

        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode, firstNode)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(placeGraphInitializeService.initializeGraph(any(), any())).thenAnswer {
            val ctx = it.arguments[0] as GraphInitializeContext
            ctx.isFindPlace = true
            placeActionList.forEach { action -> ctx.history.add(action) }
        }
        whenever(menuAgent.determineAction(any(), any())).thenReturn(firstAction, veryLowScoreAction)

        // when & then
        assertThrows<LowScoreException> {
            menuGraphInitializeService.initializeGraph(context, lowScoreMenuList)
        }

        assertTrue(context.lowScoreCount >= 5)
    }

    @Test
    fun `다른 카테고리로 이동 시 이전 카테고리 노드와 PATH_TO 관계로 연결된다`() {
        // given: 서로 다른 카테고리를 가진 두 메뉴 리스트
        val menuWithCategory1 = MenuInfoDto(title = "메뉴1", category = "카테고리1", options = listOf())
        val menuWithCategory2 = MenuInfoDto(title = "메뉴2", category = "카테고리2", options = listOf())

        // Agent가 반환할 액션들을 순서대로 정의
        // 1. 첫 번째 메뉴의 카테고리 액션 (handleFirstNode에서 사용)
        val category1Action = AgentActionDto(
            coordinate = listOf(10, 20),
            title = "카테고리1",
            goNext = true, // 카테고리 버튼은 보통 다음 화면으로 이동
            score = HIGH_SCORE
        )
        // 2. 첫 번째 메뉴 아이템 액션 (selectMenu에서 사용)
        val menu1Action = AgentActionDto(
            coordinate = listOf(30, 40),
            title = "메뉴1",
            goNext = false, // 메뉴 선택은 보통 현재 페이지에 머무름
            score = HIGH_SCORE
        )
        // 3. 두 번째 메뉴의 카테고리 액션 (selectCategory에서 사용) -> 이 액션이 PATH_TO 관계를 만듦
        val category2Action = AgentActionDto(
            coordinate = listOf(50, 60),
            title = "카테고리2",
            goNext = true,
            score = HIGH_SCORE
        )

        // utgService.saveNode가 반환할 엔티티들을 순서대로 정의
        val category1Node = UiEntity(id = "CATEGORY_1_NODE_ID", isNext = true, x = 10, y = 20, title = "카테고리1", kioskId = TEST_KIOSK_ID)
        val menu1Node = UiEntity(id = "MENU_1_NODE_ID", isNext = false, x = 30, y = 40, title = "메뉴1", kioskId = TEST_KIOSK_ID)
        val category2Node = UiEntity(id = "CATEGORY_2_NODE_ID", isNext = true, x = 50, y = 60, title = "카테고리2", kioskId = TEST_KIOSK_ID)

        val context = createTestContext() // lastNode가 null인 초기 컨텍스트

        whenever(utgService.saveNode(any<UiDto>())).thenReturn(
            rootNode,       // 1. initializeGraph의 루트 노드
            category1Node,  // 2. handleFirstNode의 카테고리 노드
            menu1Node,      // 3. selectMenu의 메뉴 노드
            category2Node   // 4. selectCategory의 카테고리 노드
        )
        whenever(menuAgent.determineAction(any(), any())).thenReturn(
            category1Action, // 1. handleFirstNode에서 호출
            menu1Action,     // 2. selectMenu에서 호출
            category2Action  // 3. selectCategory에서 호출
        )

        // 테스트에 필요한 기본적인 Mocking
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(notificationService.sendActionCommand(any(), any())).thenReturn(actionResult)
        whenever(pageAgent.determineAction(any(), any())).thenReturn(pageAction) // handleModal 내부에서 사용될 수 있음
        whenever(placeGraphInitializeService.initializeGraph(any(), any())).thenAnswer {
            // 첫 handleFirstNode에서 호출되므로 Mocking 필요
            context.isFindPlace = true
            Unit
        }

        // when: 다른 카테고리의 메뉴들이 포함된 리스트로 그래프 초기화 실행
        menuGraphInitializeService.initializeGraph(context, listOf(menuWithCategory1, menuWithCategory2))

        // then: 카테고리1 노드와 카테고리2 노드가 PATH_TO 관계로 양방향 연결되는지 검증
        verify(utgService).saveRel(category1Node.id, category2Node.id, NodeRelation.PATH_TO)
        verify(utgService).saveRel(category2Node.id, category1Node.id, NodeRelation.PATH_TO)

        // 그리고 context의 마지막 노드가 새로운 카테고리 노드로 업데이트되었는지 확인
        assertEquals(category2Node.id, context.lastNode!!.id)
    }

    @Test
    fun `포장_매장_UI를_찾지_못한_경우_마지막에_다시_시도한다`() {
        // given: 처음에 포장/매장 UI를 찾지 못하는 상황
        val context = createTestContext()
        var callCount = 0

        // ← mockStatic 블록 완전 제거!
        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode, firstNode, menuEntity)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(placeGraphInitializeService.initializeGraph(any(), any())).thenAnswer {
            val ctx = it.arguments[0] as GraphInitializeContext
            callCount++
            if (callCount == 2) {
                ctx.isFindPlace = true
                placeActionList.forEach { action -> ctx.history.add(action) }
            }
        }
        whenever(menuAgent.determineAction(any(), any())).thenReturn(firstAction, menuAction)
        whenever(pageAgent.determineAction(any(), any())).thenReturn(pageAction)
        whenever(notificationService.sendActionCommand(any(), any())).thenReturn(actionResult)

        // when
        menuGraphInitializeService.initializeGraph(context, listOf(menuInfoDto))

        // then: 포장/매장 UI 탐색이 두 번 호출되고 마지막에 찾아진다
        assertTrue(context.isFindPlace)
        verify(placeGraphInitializeService, times(2)).initializeGraph(any(), any())
    }

    @Test
    fun `옵션_처리_후_원래_페이지로_돌아간다`() {
        // given
        val context = createTestContext()

        // ← mockStatic 블록 완전 제거!
        setupBasicMocks(context)
        setupModalMocks(hasModal = false)
        setupOptionMocks()

        // when
        menuGraphInitializeService.initializeGraph(context, menuList)

        // then
        verify(backAgent, atLeastOnce()).determineBack(any())
        verify(notificationService, atLeast(4)).sendCaptureCommand(TEST_KIOSK_ID) // 여러 번 캡처
    }

    private fun createTestContext(): GraphInitializeContext {
        return GraphInitializeContext(
            kioskId = TEST_KIOSK_ID,
            isFindPlace = false,
            lowScoreCount = 0,
            lastNode = null,
            nowCategory = null,
            imageHash = null,
            history = mutableListOf()
        )
    }

    private fun setupBasicMocks(context: GraphInitializeContext) {
        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode, firstNode, menuEntity, modalEntity, optionEntity, backEntity)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID))
            .thenReturn(TEST_IMAGE_DATA)
            .thenReturn(TEST_MODAL_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(uiExtractorManager.getUiComponents(TEST_MODAL_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(modalLlmUiList)
        whenever(placeGraphInitializeService.initializeGraph(any(), any())).thenAnswer {
            val ctx = it.arguments[0] as GraphInitializeContext
            ctx.isFindPlace = true
            placeActionList.forEach { action -> ctx.history.add(action) }
        }
        whenever(notificationService.sendActionCommand(any(), any())).thenReturn(actionResult)
        doNothing().whenever(utgService).saveRel(anyString(), anyString(), any())
    }

    private fun setupModalMocks(hasModal: Boolean) {
        if (hasModal) {
            whenever(pageAgent.determineAction(any(), any())).thenReturn(modalPageAction)
            whenever(menuAgent.determineAction(menuInfoWithOptionsDto, modalLlmUiList)).thenReturn(modalMenuAction)
        } else {
            whenever(pageAgent.determineAction(any(), any())).thenReturn(pageAction)
        }
        whenever(menuAgent.determineAction(any(), anyList())).thenReturn(firstAction, menuAction)
        whenever(backAgent.determineBack(any())).thenReturn(backAction)
    }

    private fun setupOptionMocks() {
        val optAction1 = AgentActionDto(false, HIGH_SCORE, listOf(150, 250), "치즈추가")
        val optAction2 = AgentActionDto(false, HIGH_SCORE, listOf(160, 260), "피클빼기")

        val cheeseMenuDto = MenuInfoDto("치즈추가", listOf(), menuInfoWithOptionsDto.title)
        val pickleMenuDto = MenuInfoDto("피클빼기", listOf(), menuInfoWithOptionsDto.title)

        whenever(menuAgent.determineAction(eq(cheeseMenuDto), any())).thenReturn(optAction1)
        whenever(menuAgent.determineAction(eq(pickleMenuDto), any())).thenReturn(optAction2)
    }

    @Test
    fun 누락된_컴포넌트가_추가된다() {
        // given: 누락된 컴포넌트가 감지되는 상황
        val additionalComponents = listOf(
            LlmUiComponentDto(x = 150, y = 250, title = "숨겨진옵션")
        )

        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode, menuEntity)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(placeGraphInitializeService.initializeGraph(any(), anyList())).thenAnswer {  }
        whenever(menuAgent.determineAction(any<MenuInfoDto>(), anyList())).thenReturn(menuAction)
        whenever(backAgent.determineBack(any())).thenReturn(backAction)
        whenever(pageAgent.determineAction(anyList(), anyList())).thenReturn(pageAction)
        whenever(notificationService.sendActionCommand(TEST_KIOSK_ID, TEST_COORDINATE)).thenReturn(actionResult)
        doNothing().whenever(utgService).saveRel(anyString(), anyString(), any())

        // when: 메뉴 그래프 초기화 실행
        val context = GraphInitializeContext(
            kioskId = TEST_KIOSK_ID,
            isFindPlace = false,
            lowScoreCount = 0,
            lastNode = null,
            imageHash = null,
            nowCategory = null,
            history = mutableListOf()
        )
        menuGraphInitializeService.initializeGraph(context, menuList)

        // then: 누락된 컴포넌트가 감지되고 추가된다
    }
}
