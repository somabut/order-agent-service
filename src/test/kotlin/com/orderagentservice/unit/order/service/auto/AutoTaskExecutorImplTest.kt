package com.orderagentservice.unit.order.service.auto

import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.AutoOrderResultDto
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderOption
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.auto.AutoTaskExecutorImpl
import com.orderagentservice.order.service.auto.OrderLogSender
import com.orderagentservice.order.service.graph.ui.UiGraphService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@ExtendWith(MockKExtension::class)
class AutoTaskExecutorImplTest {
    @MockK
    private lateinit var graphService: UiGraphService
    @MockK
    private lateinit var notificationService: NotificationService
    @MockK(relaxed = true)
    private lateinit var orderLogSender: OrderLogSender

    private lateinit var autoTaskExecutor: AutoTaskExecutorImpl

    private val MOCK_KIOSK_ID = "k-123"
    private val MOCK_TASK_ID = "t-abc"
    private val MOCK_START_NODE_ID = "node-start"
    private val MOCK_MENU_NODE_ID = "node-menu"

    @MockK private lateinit var mockContext: AutoOrderContext
    @MockK private lateinit var mockHistory: AutoOrderResultDto
    @MockK private lateinit var mockAction1: ActionPathDto
    @MockK private lateinit var mockAction2: ActionPathDto

    private val MOCK_COORD_1 = CoordinateDto(10, 11, "Action 1")
    private val MOCK_COORD_2 = CoordinateDto(20, 22, "Action 2")

    @BeforeEach
    fun setUp() {
        autoTaskExecutor = AutoTaskExecutorImpl(
            graphService,
            notificationService,
            orderLogSender
        )

        every { mockContext.kioskId } returns MOCK_KIOSK_ID
        every { mockContext.taskId } returns MOCK_TASK_ID
        every { mockContext.nodeId } returns MOCK_START_NODE_ID
        every { mockContext.history } returns mockHistory

        every { mockAction1.x } returns 10
        every { mockAction1.y } returns 11
        every { mockAction1.title } returns "Action 1"
        every { mockAction1.id } returns "node-1"

        every { mockAction2.x } returns 20
        every { mockAction2.y } returns 22
        every { mockAction2.title } returns "Action 2"
        every { mockAction2.id } returns "node-2"

        every { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) } returns MOCK_COORD_1
        every { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_2) } returns MOCK_COORD_2
    }

    @Test
    @DisplayName("clickCategory: 경로의 모든 액션을 클릭하고 마지막 노드 ID를 반환한다")
    fun `clickCategory should click all actions and return last node id`() {
        // given
        val category = "Coffee"
        val actionList = listOf(mockAction1, mockAction2)
        every { graphService.findPath(MOCK_KIOSK_ID, MOCK_START_NODE_ID, category) } returns actionList

        // when
        val lastNodeId = autoTaskExecutor.clickCategory(mockContext, category)

        // then
        assertThat(lastNodeId).isEqualTo("node-2") // mockAction2.id
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) }
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_2) }
    }

    @Test
    @DisplayName("clickMenu: count가 3일 때, 마지막 메뉴를 3번 클릭한다")
    fun `clickMenu should click last menu 3 times when count is 3`() {
        // given
        val menu = AutoOrderMenu(category = "Coffee", title = "Americano", count = 3, autoOrderOptions = emptyList())
        val actionList = listOf(mockAction1, mockAction2) // mockAction2가 마지막 메뉴
        every { graphService.findPath(MOCK_KIOSK_ID, MOCK_START_NODE_ID, "Americano") } returns actionList

        // when
        val lastAction = autoTaskExecutor.clickMenu(mockContext, menu)

        // then
        assertThat(lastAction).isEqualTo(mockAction2)
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) }
        verify(exactly = 3) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_2) }
    }

    @Test
    @DisplayName("clickOption: 옵션 경로가 1개일 때, nodeId가 변경되지 않는다")
    fun `clickOption should not update nodeId if path size is 1`() {
        // given
        val opt1 = AutoOrderOption(title = "Size Up", count = 1)
        val options = listOf(opt1)
        val actionList = listOf(mockAction1) // 경로 1개

        every { graphService.findPath(MOCK_KIOSK_ID, MOCK_MENU_NODE_ID, "Size Up") } returns actionList

        // when
        val lastNodeId = autoTaskExecutor.clickOption(mockContext, options, MOCK_MENU_NODE_ID)

        // then
        assertThat(lastNodeId).isEqualTo(MOCK_MENU_NODE_ID)
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) }
    }

    @Test
    @DisplayName("clickOption: 옵션 경로가 2개 이상일 때, nodeId를 이전 노드 ID로 변경한다")
    fun `clickOption should update nodeId to previous node id if path size is greater than 1`() {
        // given
        val opt1 = AutoOrderOption(title = "Add Shot", count = 1)
        val options = listOf(opt1)
        val actionList = listOf(mockAction1, mockAction2) // 경로 2개

        every { graphService.findPath(MOCK_KIOSK_ID, MOCK_MENU_NODE_ID, "Add Shot") } returns actionList

        // when
        val lastNodeId = autoTaskExecutor.clickOption(mockContext, options, MOCK_MENU_NODE_ID)

        // then
        assertThat(lastNodeId).isEqualTo("node-1")
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) }
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_2) }
    }

    @Test
    @DisplayName("clickBack: 'station' 경로를 찾아 클릭하고 마지막 노드 ID를 반환한다")
    fun `clickBack should find station path and return last node id`() {
        // given
        val actionList = listOf(mockAction1, mockAction2)
        every { graphService.findPath(MOCK_KIOSK_ID, MOCK_MENU_NODE_ID, "station") } returns actionList

        // when
        val lastNodeId = autoTaskExecutor.clickBack(mockContext, MOCK_MENU_NODE_ID)

        // then
        assertThat(lastNodeId).isEqualTo("node-2")
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) }
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_2) }
    }

    @Test
    @DisplayName("clickPayment: 주어진 결제 노드를 1회 클릭한다")
    fun `clickPayment should click the given payment node once`() {
        // given
        val paymentNode = mockAction1

        // when
        autoTaskExecutor.clickPayment(mockContext, paymentNode)

        // then
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) }
    }

    @Test
    @DisplayName("clickPlace: 장소 노드를 찾으면 클릭하고 true를 반환한다")
    fun `clickPlace should click node and return true if place is found`() {
        // given
        val place = "STORE"
        val placeAction = mockAction1
        val returnedCoord = CoordinateDto(10, 11, "매장 식사") // 클릭 후 실제 타이틀

        every { mockContext.place } returns place
        every { graphService.findPlace(MOCK_KIOSK_ID, MOCK_START_NODE_ID, place) } returns placeAction
        every { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) } returns returnedCoord
        every { mockHistory.payment = any() } just runs // history.payment setter 모의

        // when
        val result = autoTaskExecutor.clickPlace(mockContext)

        // then
        assertThat(result).isTrue()
        verify(exactly = 1) { notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORD_1) }
        verify(exactly = 1) { mockHistory.payment = "매장 식사" }
    }

    @Test
    @DisplayName("clickPlace: 장소 노드를 찾지 못하면 false를 반환한다")
    fun `clickPlace should return false if place is not found`() {
        // given
        val place = "STORE"
        every { mockContext.place } returns place
        every { graphService.findPlace(MOCK_KIOSK_ID, MOCK_START_NODE_ID, place) } returns null // 노드 못 찾음

        // when
        val result = autoTaskExecutor.clickPlace(mockContext)

        // then
        assertThat(result).isFalse()
        verify(exactly = 0) { notificationService.sendActionCommand(any(), any()) }
    }
}