package com.orderagentservice.unit.order.service.auto

import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.order.service.auto.AutoOrderService
import com.orderagentservice.order.service.auto.AutoTaskExecutor
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

@ExtendWith(MockKExtension::class)
class AutoOrderServiceTest {
    @MockK
    private lateinit var graphService: UiGraphService
    @MockK(relaxed = true)
    private lateinit var globalLogger: GlobalLogger
    @MockK
    private lateinit var autoTaskExecutor: AutoTaskExecutor
    @MockK(relaxed = true)
    private lateinit var orderLogSender: OrderLogSender

    private lateinit var autoOrderService: AutoOrderService

    private val MOCK_KIOSK_ID = "k-123"
    private val MOCK_TASK_ID = "t-abc"
    private val MOCK_ROOT_NODE_ID = "root-node-id"
    private val MOCK_MENU_CLICK_NODE_ID = "menu-node-id"
    private val MOCK_OPTION_CLICK_NODE_ID = "option-node-id"
    private val MOCK_CATEGORY_NODE_ID = "category-node-id"
    private val MOCK_BACK_NODE_ID = "back-node-id"

    @MockK private lateinit var rootNode: ActionPathDto
    @MockK private lateinit var menuClickNode: ActionPathDto
    @MockK private lateinit var paymentAction1: ActionPathDto
    @MockK private lateinit var paymentAction2: ActionPathDto
    @MockK private lateinit var completeAction: ActionPathDto

    @BeforeEach
    fun setUp() {
        // 테스트 대상 클래스 수동 주입
        autoOrderService = AutoOrderService(
            graphService = graphService,
            globalLogger = globalLogger,
            autoTaskExecutor = autoTaskExecutor,
            orderLogSender = orderLogSender
        )

        every { rootNode.id } returns MOCK_ROOT_NODE_ID
        every { menuClickNode.id } returns MOCK_MENU_CLICK_NODE_ID
    }

    @Test
    @DisplayName("execute: 단일 메뉴(옵션X, 모달X) 주문 시, 모든 단계를 순차 실행한다")
    fun `execute should process a single menu item without options successfully`() {
        // given
        val menu = AutoOrderMenu(category = "Coffee", title = "Americano", autoOrderOptions = emptyList(), count = 0)
        val orderRequest = AutoOrderRequest(place = "STORE", autoOrderMenus = listOf(menu), payment = "CARD")
        val paymentPath = listOf(paymentAction1, paymentAction2, completeAction)

        every { graphService.findRoot(MOCK_KIOSK_ID) } returns rootNode


        every { autoTaskExecutor.clickPlace(any()) } returns true
        every { autoTaskExecutor.clickMenu(any(), eq(menu)) } returns menuClickNode
        every { graphService.isBackRel(MOCK_KIOSK_ID, MOCK_MENU_CLICK_NODE_ID) } returns false
        every { autoTaskExecutor.clickOption(any(), eq(emptyList()), eq(MOCK_MENU_CLICK_NODE_ID)) } returns MOCK_MENU_CLICK_NODE_ID // 동일 노드 ID 반환
        every { graphService.findNodeByTitle(MOCK_KIOSK_ID, menu.category) } returns MOCK_CATEGORY_NODE_ID

        every { graphService.findPath(MOCK_KIOSK_ID, MOCK_CATEGORY_NODE_ID, "complete") } returns paymentPath
        every { autoTaskExecutor.clickPayment(any(), eq(paymentAction1)) } just runs
        every { autoTaskExecutor.clickPayment(any(), eq(paymentAction2)) } just runs

        // when
        val resultDto = autoOrderService.execute(MOCK_KIOSK_ID, MOCK_TASK_ID, orderRequest)

        // then
        assertThat(resultDto).isNotNull
        assertThat(resultDto.processingTime).isGreaterThanOrEqualTo(0)

        verify(exactly = 1) {
            globalLogger.loggingOrderStart(MOCK_KIOSK_ID, MOCK_TASK_ID)
            graphService.findRoot(MOCK_KIOSK_ID)
            autoTaskExecutor.clickPlace(any())
            autoTaskExecutor.clickMenu(any(), menu)
            graphService.isBackRel(MOCK_KIOSK_ID, MOCK_MENU_CLICK_NODE_ID)
            autoTaskExecutor.clickOption(any(), emptyList(), MOCK_MENU_CLICK_NODE_ID)
            graphService.findNodeByTitle(MOCK_KIOSK_ID, menu.category)
            graphService.findPath(MOCK_KIOSK_ID, MOCK_CATEGORY_NODE_ID, "complete")
            autoTaskExecutor.clickPayment(any(), paymentAction1)
            autoTaskExecutor.clickPayment(any(), paymentAction2)
            globalLogger.loggingOrderResult(eq(MOCK_KIOSK_ID), any(), any(), eq(orderRequest.payment), eq(MOCK_TASK_ID))
        }

        verify(exactly = 0) {
            autoTaskExecutor.clickBack(any(), any()) // isBackRel=false 였으므로
            autoTaskExecutor.clickPayment(any(), completeAction) // removeLast()
        }
    }

    @Test
    @DisplayName("execute: 메뉴 목록이 비어있을 시, proceedPayment에서 clickPlace를 호출한다")
    fun `execute should skip putMenus and call clickPlace in proceedPayment if menu list is empty`() {
        // given
        val orderRequest = AutoOrderRequest(place = "STORE", autoOrderMenus = emptyList(), payment = "CARD")
        val paymentPath = listOf(paymentAction1, completeAction)

        every { graphService.findRoot(MOCK_KIOSK_ID) } returns rootNode
        every { graphService.findPath(MOCK_KIOSK_ID, MOCK_ROOT_NODE_ID, "complete") } returns paymentPath
        every { autoTaskExecutor.clickPlace(any()) } returns true
        every { autoTaskExecutor.clickPayment(any(), eq(paymentAction1)) } just runs

        // when
        autoOrderService.execute(MOCK_KIOSK_ID, MOCK_TASK_ID, orderRequest)

        // then
        verify(exactly = 1) {
            autoTaskExecutor.clickPlace(any())
        }
        verify(exactly = 0) {
            autoTaskExecutor.clickMenu(any(), any())
            autoTaskExecutor.clickOption(any(), any(), any())
            autoTaskExecutor.clickBack(any(), any())
        }
    }
}