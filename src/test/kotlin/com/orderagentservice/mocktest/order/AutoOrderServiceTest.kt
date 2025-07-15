package com.orderagentservice.mocktest.order

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.OrderResultDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderOption
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.order.service.AutoOrderService
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.UtgService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class AutoOrderServiceTest {
    companion object {
        private const val TEST_KIOSK_ID = "KIOSK_001"
        private const val TEST_ROOT_NODE_ID = "root"
        private const val TEST_MENU_ID = "menu1"
        private const val TEST_OPTION_ID = "opt1"
        private const val TEST_MENU_ID_2 = "menu2"
        private const val TEST_CATEGORY_ID = "category1"
        private const val TEST_CATEGORY_ID_2 = "category2"
        private const val TEST_COMPLETE_ID = "complete"
        private const val TEST_PLACE_ID = "place1"
        private const val TEST_BACK_ID = "back1"

        private const val TEST_X_COORDINATE = 100
        private const val TEST_Y_COORDINATE = 200
        private const val TEST_X_COORDINATE_2 = 150
        private const val TEST_Y_COORDINATE_2 = 250
        private const val TEST_X_COORDINATE_3 = 300
        private const val TEST_Y_COORDINATE_3 = 400

        private const val TEST_MENU_TITLE = "아메리카노"
        private const val TEST_MENU_TITLE_2 = "카페라떼"
        private const val TEST_MENU_TITLE_3 = "샌드위치"
        private const val TEST_OPTION_TITLE = "HOT"
        private const val TEST_CATEGORY = "음료"
        private const val TEST_CATEGORY_2 = "푸드"
        private const val TEST_PLACE_STORE = "매장"
        private const val TEST_PLACE_TAKEOUT = "포장"
        private const val TEST_PAYMENT_CARD = "CARD"
        private const val TEST_PAYMENT_CASH = "CASH"
        private const val TEST_COMPLETE_TITLE = "complete"
        private const val TEST_BACK_TITLE = "뒤로가기"

        private const val TEST_MENU_COUNT = 2
        private const val TEST_MENU_COUNT_1 = 1
        private const val TEST_OPTION_COUNT = 1
    }

    @Mock
    private lateinit var notificationService: NotificationService

    @Mock
    private lateinit var utgService: UtgService

    @Mock
    private lateinit var globalLogger: GlobalLogger

    private lateinit var autoOrderService: AutoOrderService
    private lateinit var orderRequest: AutoOrderRequest
    private lateinit var mockActionList: List<AgentActionDto>
    private lateinit var mockPath: ActionPathDto
    private lateinit var mockOpt: ActionPathDto
    private lateinit var mockRoot: ActionPathDto
    private lateinit var mockTakePlace: ActionPathDto
    private lateinit var mockDinePlace: ActionPathDto
    private lateinit var mockPathList: List<ActionPathDto>
    private lateinit var mockBackPathList: List<ActionPathDto>

    private lateinit var mockOptionDto: CoordinateDto
    private lateinit var mockBackList: List<AgentBackDto>
    private lateinit var mockPlaceAction: AgentActionDto

    @BeforeEach
    fun setUp() {
        autoOrderService = AutoOrderService(notificationService, utgService, globalLogger)

        orderRequest = AutoOrderRequest(
            place = TEST_PLACE_STORE,
            payment = TEST_PAYMENT_CARD,
            autoOrderMenus = listOf(
                AutoOrderMenu(
                    title = TEST_MENU_TITLE,
                    category = TEST_CATEGORY,
                    count = TEST_MENU_COUNT,
                    autoOrderOptions = listOf(
                        AutoOrderOption(title = TEST_OPTION_TITLE, count = TEST_OPTION_COUNT)
                    )
                )
            )
        )

        mockActionList = listOf(
            AgentActionDto(title = TEST_MENU_TITLE, goNext = false, score = 0.9F, coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE))
        )

        mockOptionDto = CoordinateDto(TEST_X_COORDINATE_2, TEST_Y_COORDINATE_2, TEST_OPTION_TITLE)

        mockBackList = listOf(
            AgentBackDto(title = TEST_BACK_TITLE, score = 0.9F, coordinate = listOf(TEST_X_COORDINATE, TEST_Y_COORDINATE))
        )

        mockBackPathList = listOf(
            ActionPathDto(id = TEST_BACK_ID, x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_BACK_TITLE),
        )

        mockPath = ActionPathDto(id = TEST_MENU_ID, x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_MENU_TITLE)
        mockOpt = ActionPathDto(id = TEST_OPTION_ID, x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_OPTION_TITLE)
        mockRoot = ActionPathDto(id = TEST_ROOT_NODE_ID, x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = "root")
        mockTakePlace = ActionPathDto(id = TEST_PLACE_ID, x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_PLACE_TAKEOUT)
        mockDinePlace = ActionPathDto(id = TEST_PLACE_ID, x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_PLACE_STORE)
        mockPathList = listOf(mockPath)

        mockPlaceAction = AgentActionDto(title = TEST_MENU_TITLE, goNext = false, score = 0.9F, coordinate = listOf(
            TEST_X_COORDINATE, TEST_Y_COORDINATE
        ))
    }

    @Test
    fun `자동주문이_정상적으로_완료된다`() {
        // given: 정상적인 주문 요청과 Mock 설정
        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_ROOT_NODE_ID, TEST_MENU_TITLE)).thenReturn(mockPathList)
        whenever(utgService.findOptionNode(TEST_KIOSK_ID, TEST_MENU_ID, TEST_OPTION_TITLE)).thenReturn(mockOpt)
        whenever(utgService.findBackPath(TEST_KIOSK_ID, TEST_MENU_ID)).thenReturn(mockBackPathList)
        whenever(utgService.findCategoryNodeId(TEST_KIOSK_ID, TEST_MENU_ID)).thenReturn(TEST_CATEGORY_ID)
        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_CATEGORY_ID, TEST_COMPLETE_TITLE)).thenReturn(mockPathList)
        whenever(utgService.findRootNodeId(TEST_KIOSK_ID)).thenReturn(mockRoot)
        doNothing().whenever(globalLogger).loggingOrderStart(TEST_KIOSK_ID)
        doNothing().whenever(globalLogger).loggingOrderResult(eq(TEST_KIOSK_ID), any(), any(), eq(TEST_PAYMENT_CARD))

        // when: 자동주문 진행
        autoOrderService.proceed(TEST_KIOSK_ID, orderRequest)

        // then: 로그 기록 및 서비스 호출이 정상적으로 수행된다
        verify(globalLogger).loggingOrderStart(TEST_KIOSK_ID)
        verify(globalLogger).loggingOrderResult(eq(TEST_KIOSK_ID), any(), any(), eq(TEST_PAYMENT_CARD))

        val coordinateCaptor = argumentCaptor<CoordinateDto>()
        verify(notificationService, atLeast(3)).sendActionCommand(eq(TEST_KIOSK_ID), coordinateCaptor.capture())

        val capturedCoordinates = coordinateCaptor.allValues
        println(capturedCoordinates)
        assertEquals(TEST_MENU_TITLE, capturedCoordinates[0].title)
        assertEquals(TEST_OPTION_TITLE, capturedCoordinates[2].title)

        verify(utgService).findMenuPath(TEST_KIOSK_ID, TEST_ROOT_NODE_ID, TEST_MENU_TITLE)
        verify(utgService).findOptionNode(TEST_KIOSK_ID, TEST_MENU_ID, TEST_OPTION_TITLE)
        verify(utgService).findBackPath(TEST_KIOSK_ID, TEST_MENU_ID)
    }

    @Test
    fun `포장주문시_포장_노드를_클릭한다`() {
        // given: 포장 주문 요청과 Mock 설정
        val takeoutOrderRequest = AutoOrderRequest(
            place = TEST_PLACE_TAKEOUT,
            payment = TEST_PAYMENT_CASH,
            autoOrderMenus = listOf(
                AutoOrderMenu(
                    title = TEST_MENU_TITLE_2,
                    category = TEST_CATEGORY,
                    count = TEST_MENU_COUNT_1,
                    autoOrderOptions = emptyList()
                )
            )
        )

        val mockTakeoutActionList = listOf(
            ActionPathDto(title = TEST_MENU_TITLE_2, id = TEST_MENU_ID, x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE),
        )

        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_ROOT_NODE_ID, TEST_MENU_TITLE_2)).thenReturn(mockTakeoutActionList)
        whenever(utgService.findPlaceNodeId(TEST_KIOSK_ID, TEST_ROOT_NODE_ID, TEST_PLACE_TAKEOUT)).thenReturn(mockTakePlace)
        whenever(utgService.findCategoryNodeId(TEST_KIOSK_ID, TEST_MENU_ID)).thenReturn(TEST_CATEGORY_ID)
        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_CATEGORY_ID, TEST_COMPLETE_TITLE)).thenReturn(
            listOf(ActionPathDto(id = TEST_COMPLETE_ID, x = TEST_X_COORDINATE_3, y = TEST_Y_COORDINATE_3, title = TEST_COMPLETE_TITLE))
        )
        whenever(utgService.findRootNodeId(TEST_KIOSK_ID)).thenReturn(mockRoot)
        doNothing().whenever(globalLogger).loggingOrderStart(TEST_KIOSK_ID)
        doNothing().whenever(globalLogger).loggingOrderResult(eq(TEST_KIOSK_ID), any(), any(), eq(TEST_PAYMENT_CASH))

        // when: 자동주문 진행
        autoOrderService.proceed(TEST_KIOSK_ID, takeoutOrderRequest)

        // then: 포장 노드 클릭이 정상적으로 수행된다
        verify(utgService).findPlaceNodeId(TEST_KIOSK_ID, TEST_ROOT_NODE_ID, TEST_PLACE_TAKEOUT)
        verify(notificationService).sendActionCommand(TEST_KIOSK_ID, CoordinateDto(TEST_X_COORDINATE, TEST_Y_COORDINATE, TEST_PLACE_TAKEOUT))
        verify(globalLogger).loggingOrderStart(TEST_KIOSK_ID)
        verify(globalLogger).loggingOrderResult(eq(TEST_KIOSK_ID), any(), any(), eq(TEST_PAYMENT_CASH))
    }

    @Test
    fun `여러개_메뉴를_주문할_때_각각_처리된다`() {
        // given: 여러 메뉴가 포함된 주문 요청
        val multiMenuOrderRequest = AutoOrderRequest(
            place = null,
            payment = TEST_PAYMENT_CARD,
            autoOrderMenus = listOf(
                AutoOrderMenu(
                    title = TEST_MENU_TITLE,
                    category = TEST_CATEGORY,
                    count = TEST_MENU_COUNT_1,
                    autoOrderOptions = emptyList()
                ),
                AutoOrderMenu(
                    title = TEST_MENU_TITLE_3,
                    category = TEST_CATEGORY_2,
                    count = TEST_MENU_COUNT_1,
                    autoOrderOptions = emptyList()
                )
            )
        )

        val mockActionList1 = listOf(ActionPathDto(id = TEST_MENU_ID, x = TEST_X_COORDINATE, y = TEST_Y_COORDINATE, title = TEST_MENU_TITLE))
        val mockActionList2 = listOf(ActionPathDto(id = TEST_MENU_ID_2, x = TEST_X_COORDINATE_2, y = TEST_Y_COORDINATE_2, title = TEST_MENU_TITLE_3))

        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_ROOT_NODE_ID, TEST_MENU_TITLE)).thenReturn(mockActionList1)
        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_CATEGORY_ID, TEST_MENU_TITLE_3)).thenReturn(mockActionList2)
        whenever(utgService.findCategoryNodeId(TEST_KIOSK_ID, TEST_MENU_ID)).thenReturn(TEST_CATEGORY_ID)
        whenever(utgService.findCategoryNodeId(TEST_KIOSK_ID, TEST_MENU_ID_2)).thenReturn(TEST_CATEGORY_ID_2)
        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_CATEGORY_ID_2, TEST_COMPLETE_TITLE)).thenReturn(
            listOf(ActionPathDto(id = TEST_COMPLETE_ID, x = TEST_X_COORDINATE_3, y = TEST_Y_COORDINATE_3, title = TEST_COMPLETE_TITLE))
        )
        whenever(utgService.findRootNodeId(TEST_KIOSK_ID)).thenReturn(mockRoot)
        doNothing().whenever(globalLogger).loggingOrderStart(TEST_KIOSK_ID)
        doNothing().whenever(globalLogger).loggingOrderResult(eq(TEST_KIOSK_ID), any(), any(), eq(TEST_PAYMENT_CARD))

        // when: 자동주문 진행
        autoOrderService.proceed(TEST_KIOSK_ID, multiMenuOrderRequest)

        // then: 각 메뉴가 개별적으로 처리되고 결과에 2개 메뉴가 포함된다
        verify(utgService).findMenuPath(TEST_KIOSK_ID, TEST_ROOT_NODE_ID, TEST_MENU_TITLE)
        verify(utgService).findMenuPath(TEST_KIOSK_ID, TEST_CATEGORY_ID, TEST_MENU_TITLE_3)
        verify(notificationService).sendActionCommand(TEST_KIOSK_ID, CoordinateDto(TEST_X_COORDINATE, TEST_Y_COORDINATE, TEST_MENU_TITLE))
        verify(notificationService).sendActionCommand(TEST_KIOSK_ID, CoordinateDto(TEST_X_COORDINATE_2, TEST_Y_COORDINATE_2, TEST_MENU_TITLE_3))

        val historyCaptor = argumentCaptor<List<OrderResultDto>>()
        verify(globalLogger).loggingOrderResult(eq(TEST_KIOSK_ID), historyCaptor.capture(), any(), eq(TEST_PAYMENT_CARD))
        assertEquals(2, historyCaptor.firstValue.size)
        assertEquals(TEST_MENU_TITLE, historyCaptor.firstValue[0].title)
        assertEquals(TEST_MENU_TITLE_3, historyCaptor.firstValue[1].title)
    }

    @Test
    fun `옵션이_있는_메뉴_주문시_옵션_선택_후_뒤로가기한다`() {
        // given: 옵션이 있는 메뉴 주문 요청
        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_ROOT_NODE_ID, TEST_MENU_TITLE)).thenReturn(mockPathList)
        whenever(utgService.findOptionNode(TEST_KIOSK_ID, TEST_MENU_ID, TEST_OPTION_TITLE)).thenReturn(mockOpt)
        whenever(utgService.findBackPath(TEST_KIOSK_ID, TEST_MENU_ID)).thenReturn(mockBackPathList)
        whenever(utgService.findCategoryNodeId(TEST_KIOSK_ID, TEST_MENU_ID)).thenReturn(TEST_CATEGORY_ID)
        whenever(utgService.findMenuPath(TEST_KIOSK_ID, TEST_CATEGORY_ID, TEST_COMPLETE_TITLE)).thenReturn(
            listOf(ActionPathDto(id = TEST_COMPLETE_ID, x = TEST_X_COORDINATE_3, y = TEST_Y_COORDINATE_3, title = TEST_COMPLETE_TITLE))
        )
        whenever(utgService.findRootNodeId(TEST_KIOSK_ID)).thenReturn(mockRoot)
        doNothing().whenever(globalLogger).loggingOrderStart(TEST_KIOSK_ID)
        doNothing().whenever(globalLogger).loggingOrderResult(eq(TEST_KIOSK_ID), any(), any(), eq(TEST_PAYMENT_CARD))

        // when: 자동주문 진행
        autoOrderService.proceed(TEST_KIOSK_ID, orderRequest)

        // then: 옵션 선택 후 뒤로가기가 정상적으로 수행된다
        verify(utgService).findOptionNode(TEST_KIOSK_ID, TEST_MENU_ID, TEST_OPTION_TITLE)
        verify(utgService).findBackPath(TEST_KIOSK_ID, TEST_MENU_ID)
        verify(notificationService).sendActionCommand(TEST_KIOSK_ID, CoordinateDto(TEST_X_COORDINATE, TEST_Y_COORDINATE, TEST_OPTION_TITLE))
        verify(notificationService).sendActionCommand(TEST_KIOSK_ID, CoordinateDto(TEST_X_COORDINATE, TEST_Y_COORDINATE, TEST_BACK_TITLE))

        val historyCaptor = argumentCaptor<List<OrderResultDto>>()
        verify(globalLogger).loggingOrderResult(eq(TEST_KIOSK_ID), historyCaptor.capture(), any(), eq(TEST_PAYMENT_CARD))
        assertTrue(historyCaptor.firstValue[0].options.contains(TEST_OPTION_TITLE))
    }
}