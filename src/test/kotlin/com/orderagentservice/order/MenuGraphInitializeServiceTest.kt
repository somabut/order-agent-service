package com.orderagentservice.order

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.MissingComponentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.order.PlaceGraphInitializeServiceTest.Companion
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.service.MenuGraphInitializeService
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.PlaceGraphInitializeService
import com.orderagentservice.order.service.UtgService
import com.orderagentservice.order.util.UiExtractorManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class MenuGraphInitializeServiceTest {
    companion object {
        private const val TEST_KIOSK_ID = "KIOSK_123123"
        private const val TEST_ROOT_NODE_ID = "ROOT_NODE_123"
        private const val TEST_MENU_ENTITY_ID = "MENU_ENTITY_123"
        private const val TEST_OPTION_ENTITY_ID = "OPTION_ENTITY_123"
        private const val TEST_BACK_ENTITY_ID = "BACK_ENTITY_123"
        private const val TEST_X_COORDINATE = 100
        private const val TEST_Y_COORDINATE = 200
        private const val TEST_MENU_TITLE = "치킨버거"
        private const val TEST_OPT_TITLE = "치즈추가"
        private const val TEST_CATEGORY = "버거"
        private val TEST_IMAGE_DATA = File("image_data")
        private const val HIGH_SCORE = 0.8F
        private const val LOW_SCORE = 0.5F
    }

    private lateinit var menuAgent: MenuAgent
    private lateinit var backAgent: BackAgent
    private lateinit var missingComponentAgent: MissingComponentAgent
    private lateinit var placeGraphInitializeService: PlaceGraphInitializeService
    private lateinit var uiExtractorManager: UiExtractorManager
    private lateinit var notificationService: NotificationService
    private lateinit var utgService: UtgService
    private lateinit var menuGraphInitializeService: MenuGraphInitializeService

    private lateinit var menuList: List<MenuInfoDto>
    private lateinit var optList: List<MenuInfoDto>
    private lateinit var menuInfoDto: MenuInfoDto
    private lateinit var optInfoDto: MenuInfoDto
    private lateinit var rootNode: UiEntity
    private lateinit var menuEntity: UiEntity
    private lateinit var optionEntity: UiEntity
    private lateinit var backEntity: UiEntity
    private lateinit var llmUiList: MutableList<LlmUiComponentDto>
    private lateinit var menuAction: AgentActionDto
    private lateinit var optAction: AgentActionDto
    private lateinit var backAction: AgentBackDto
    private lateinit var placeActionList: List<AgentActionDto>
    private lateinit var actionResult: Pair<Int, Int>

    @BeforeEach
    fun setUp() {
        menuAgent = mock()
        backAgent = mock()
        missingComponentAgent = mock()
        placeGraphInitializeService = mock()
        uiExtractorManager = mock()
        notificationService = mock()
        utgService = mock()
        menuGraphInitializeService = MenuGraphInitializeService(
            menuAgent, backAgent, missingComponentAgent, placeGraphInitializeService,
            uiExtractorManager, notificationService, utgService
        )

        menuInfoDto = MenuInfoDto(
            title = TEST_MENU_TITLE,
            options = listOf("치즈추가"),
            category = TEST_CATEGORY
        )

        optInfoDto = MenuInfoDto(
            title = TEST_OPT_TITLE,
            options = listOf(),
            category = TEST_MENU_TITLE
        )

        menuList = listOf(menuInfoDto)
        optList = listOf(
            MenuInfoDto(title = "치즈추가", options = listOf(), category = TEST_CATEGORY),
            MenuInfoDto(title = "피클빼기", options = listOf(), category = TEST_CATEGORY),
        )

        rootNode = UiEntity(
            id = TEST_ROOT_NODE_ID,
            isNext = true,
            x = -1,
            y = -1,
            title = "root",
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
            LlmUiComponentDto(
                x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_MENU_TITLE,
            )
        )

        menuAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = TEST_MENU_TITLE,
            goNext = false,
            score = HIGH_SCORE
        )

        optAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = TEST_OPT_TITLE,
            goNext = false,
            score = HIGH_SCORE
        )

        backAction = AgentBackDto(
            coordinate = listOf(50, 50),
            title = "뒤로가기",
            score = HIGH_SCORE
        )

        placeActionList = listOf(
            AgentActionDto(false, 0.9F, listOf(150, 250), "포장"),
            AgentActionDto(false, 0.8F, listOf(100, 200), "매장")
        )

        actionResult = Pair(TEST_X_COORDINATE, TEST_Y_COORDINATE)

        reset(menuAgent, backAgent, missingComponentAgent, placeGraphInitializeService,
            uiExtractorManager, notificationService, utgService)
    }

    @Test
    fun `메뉴 그래프 초기화가 성공한다`() {
        // given: 정상적인 메뉴 데이터와 높은 점수의 액션

        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode).thenReturn(menuEntity)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(placeGraphInitializeService.initializeGraph(TEST_KIOSK_ID, rootNode, llmUiList)).thenReturn(placeActionList)
        whenever(menuAgent.determineAction(menuInfoDto, llmUiList)).thenReturn(menuAction)
        whenever(menuAgent.determineAction(optInfoDto, llmUiList)).thenReturn(optAction)

        whenever(missingComponentAgent.determineAction(TEST_IMAGE_DATA, menuInfoDto.options, llmUiList)).thenReturn(emptyList())
        whenever(backAgent.determineBack(any())).thenReturn(backAction)
        whenever(notificationService.sendActionCommand(TEST_KIOSK_ID, listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE))).thenReturn(actionResult)
        doNothing().whenever(utgService).saveRel(anyString(), anyString(), any())

        // when: 메뉴 그래프 초기화 실행
        val result = menuGraphInitializeService.initializeGraph(TEST_KIOSK_ID, menuList)
        println(result)

        // then: 메뉴 그래프가 성공적으로 생성되고 포장/매장 액션이 히스토리에 포함된다
        assertEquals(5, result.actionList.size)
        assertEquals(placeActionList[0], result.actionList[0])
        assertEquals(placeActionList[1], result.actionList[1])
        assertEquals(menuAction, result.actionList[2])
        assertEquals(optAction, result.actionList[3])
        assertEquals(backAction.toActionDto(), result.actionList[4])
        assertEquals(TEST_ROOT_NODE_ID, result.lastNode.id)
        assertTrue(result.isFindPlace)

        val captor = argumentCaptor<UiDto>()
        verify(utgService, times(4)).saveNode(captor.capture())
        val calls = captor.allValues
        assertEquals("root", calls[0].title)
        assertEquals(TEST_MENU_TITLE, calls[1].title)

        verify(notificationService).sendActionCommand(TEST_KIOSK_ID, menuAction.coordinate)
        verify(utgService).saveRel(TEST_ROOT_NODE_ID, TEST_MENU_ENTITY_ID, NodeRelation.HAS_TO)
    }

    @Test
    fun `낮은_점수의_액션은_노드_생성을_건너뛴다`() {
        // given: 낮은 점수의 메뉴 액션
        val lowScoreAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = TEST_MENU_TITLE,
            goNext = false,
            score = LOW_SCORE
        )
        val highScoreAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = TEST_MENU_TITLE,
            goNext = false,
            score = HIGH_SCORE
        )
        val lowMenuDto = MenuInfoDto(
            title = TEST_MENU_TITLE,
            options = listOf(),
            category = TEST_CATEGORY
        )
        val lowMenuList = listOf(lowMenuDto)

        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode).thenReturn(menuEntity)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(placeGraphInitializeService.initializeGraph(TEST_KIOSK_ID, rootNode, llmUiList)).thenReturn(emptyList())
        whenever(menuAgent.determineAction(any<MenuInfoDto>(), anyList())).thenReturn(lowScoreAction).thenReturn(highScoreAction)
        whenever(missingComponentAgent.determineAction(TEST_IMAGE_DATA, lowMenuDto.options, llmUiList)).thenReturn(emptyList())
        whenever(backAgent.determineBack(any())).thenReturn(backAction)
        whenever(notificationService.sendActionCommand(TEST_KIOSK_ID, listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE))).thenReturn(actionResult)
        doNothing().whenever(utgService).saveRel(anyString(), anyString(), any())

        // when: 메뉴 그래프 초기화 실행
        val result = menuGraphInitializeService.initializeGraph(TEST_KIOSK_ID, lowMenuList)

        // then: 낮은 점수 액션은 히스토리에 포함되지만 노드는 생성되지 않는다
        assertEquals(3, result.actionList.size) // 낮은 점수 + 높은 점수 + back
        assertEquals(lowScoreAction, result.actionList[0])
        assertEquals(highScoreAction, result.actionList[1])

        verify(menuAgent, times(2)).determineAction(lowMenuDto, llmUiList)
        verify(utgService, times(3)).saveNode(any<UiDto>()) // root + 높은 점수 액션만
    }

    @Test
    fun 다음_페이지로_이동하는_메뉴는_PATH_TO_관계로_연결된다() {
        // given: 다음 페이지로 이동하는 메뉴 액션
        val nextPageAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = "다음페이지",
            goNext = true,
            score = HIGH_SCORE
        )
        val lastPageAction = AgentActionDto(
            coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE),
            title = "끝",
            goNext = false,
            score = HIGH_SCORE
        )
        val nextPageEntity = UiEntity(
            id = "NEXT_PAGE_ENTITY",
            isNext = true,
            x = TEST_X_COORDINATE,
            y = TEST_Y_COORDINATE,
            title = "다음페이지",
            kioskId = TEST_KIOSK_ID
        )

        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode).thenReturn(nextPageEntity)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(placeGraphInitializeService.initializeGraph(TEST_KIOSK_ID, rootNode, llmUiList)).thenReturn(emptyList())
        whenever(menuAgent.determineAction(any<MenuInfoDto>(), anyList())).thenReturn(nextPageAction).thenReturn(lastPageAction)
        whenever(missingComponentAgent.determineAction(TEST_IMAGE_DATA, menuInfoDto.options, llmUiList)).thenReturn(emptyList())
        whenever(backAgent.determineBack(any())).thenReturn(backAction)
        whenever(notificationService.sendActionCommand(TEST_KIOSK_ID, listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE))).thenReturn(actionResult)
        doNothing().whenever(utgService).saveRel(anyString(), anyString(), any())

        // when: 메뉴 그래프 초기화 실행
        menuGraphInitializeService.initializeGraph(TEST_KIOSK_ID, menuList)

        // then: PATH_TO 관계로 양방향 연결된다
        verify(utgService).saveRel(TEST_ROOT_NODE_ID, "NEXT_PAGE_ENTITY", NodeRelation.PATH_TO)
        verify(utgService).saveRel("NEXT_PAGE_ENTITY", TEST_ROOT_NODE_ID, NodeRelation.PATH_TO)
    }

    @Test
    fun 포장_매장_UI를_찾지_못한_경우_마지막에_다시_시도한다() {
        // given: 처음에 포장/매장 UI를 찾지 못하는 상황
        val placeActions = listOf(
            AgentActionDto(true, 0.9F, listOf(150, 250), "포장"),
            AgentActionDto(true, 0.9F, listOf(300, 250), "매장"),
        )

        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode).thenReturn(menuEntity)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(placeGraphInitializeService.initializeGraph(TEST_KIOSK_ID, rootNode, llmUiList)).thenReturn(emptyList()).thenReturn(placeActions)
        whenever(menuAgent.determineAction(any<MenuInfoDto>(), anyList())).thenReturn(menuAction)
        whenever(missingComponentAgent.determineAction(TEST_IMAGE_DATA, menuInfoDto.options, llmUiList)).thenReturn(emptyList())
        whenever(backAgent.determineBack(any())).thenReturn(backAction)
        whenever(notificationService.sendActionCommand(TEST_KIOSK_ID, listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE))).thenReturn(actionResult)
        doNothing().whenever(utgService).saveRel(anyString(), anyString(), any())

        // when: 메뉴 그래프 초기화 실행
        val result = menuGraphInitializeService.initializeGraph(TEST_KIOSK_ID, menuList)

        // then: 포장/매장 UI 탐색이 두 번 호출되고 마지막에 찾아진다
        assertEquals(5, result.actionList.size) // 메뉴 + 포장 + back + 매장 + back
        assertTrue(result.isFindPlace)

        verify(placeGraphInitializeService, times(2)).initializeGraph(anyString(), any(), any())
    }

    @Test
    fun 누락된_컴포넌트가_추가된다() {
        // given: 누락된 컴포넌트가 감지되는 상황
        val additionalComponents = listOf(
            LlmUiComponentDto(x = 150, y = 250, title = "숨겨진옵션")
        )

        whenever(utgService.saveNode(any<UiDto>())).thenReturn(rootNode).thenReturn(menuEntity)
        whenever(notificationService.sendCaptureCommand(TEST_KIOSK_ID)).thenReturn(TEST_IMAGE_DATA)
        whenever(uiExtractorManager.getUiComponents(TEST_IMAGE_DATA, TEST_KIOSK_ID)).thenReturn(llmUiList)
        whenever(placeGraphInitializeService.initializeGraph(TEST_KIOSK_ID, rootNode, llmUiList)).thenReturn(emptyList())
        whenever(menuAgent.determineAction(any<MenuInfoDto>(), anyList())).thenReturn(menuAction)
        whenever(missingComponentAgent.determineAction(TEST_IMAGE_DATA, menuInfoDto.options, llmUiList)).thenReturn(additionalComponents)
        whenever(backAgent.determineBack(any())).thenReturn(backAction)
        whenever(notificationService.sendActionCommand(TEST_KIOSK_ID, listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE))).thenReturn(actionResult)
        doNothing().whenever(utgService).saveRel(anyString(), anyString(), any())

        // when: 메뉴 그래프 초기화 실행
        menuGraphInitializeService.initializeGraph(TEST_KIOSK_ID, menuList)

        // then: 누락된 컴포넌트가 감지되고 추가된다
        verify(missingComponentAgent).determineAction(TEST_IMAGE_DATA, menuInfoDto.options, llmUiList)
    }
}