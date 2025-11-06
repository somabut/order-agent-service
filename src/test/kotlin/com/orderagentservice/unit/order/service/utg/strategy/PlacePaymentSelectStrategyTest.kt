package com.orderagentservice.unit.order.service.utg.strategy

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.payment.PaymentNodeIntegrator
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import com.orderagentservice.order.service.utg.strategy.PlacePaymentSelectStrategy
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExtendWith(MockKExtension::class)
class PlacePaymentSelectStrategyTest {
    @MockK
    private lateinit var paymentAgent: PaymentAgent
    @MockK
    private lateinit var paymentNodeIntegrator: PaymentNodeIntegrator
    @MockK
    private lateinit var notificationService: NotificationService
    @MockK
    private lateinit var placeUtgService: PlaceUtgService // ⭐️ 추가된 의존성

    private lateinit var strategy: PlacePaymentSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = PlacePaymentSelectStrategy(
            paymentAgent,
            paymentNodeIntegrator,
            notificationService,
            placeUtgService // ⭐️ 주입
        )
    }

    @Test
    @DisplayName("execute: isPlaceDetermined가 false일 때, initializeGraph 호출 후 노드 설정")
    fun `execute should call initializeGraph and setup node when isPlaceDetermined is false`() {
        // given
        val kioskId = "kiosk-001"
        val context = mockk<UtgContext>()
        every { context.kioskId } returns kioskId
        every { context.isPlaceDetermined } returns false // ⭐️ 분기 1: false

        val uiList = listOf(mockk<UiComponentDto>())

        val mockAction = mockk<AgentActionDto>()
        val mockCoordinateResponse = mockk<CoordinateDto>()

        every { paymentAgent.determineAction(uiList) } returns mockAction
        every { mockAction.goNext } returns true
        every { mockAction.coordinate } returns listOf(10, 20)
        every { mockAction.title } returns "결제하기"

        every { placeUtgService.initializeGraph(context, uiList) } just runs

        every { paymentNodeIntegrator.integratePaymentNode(mockAction, context) } just runs
        every { notificationService.sendActionCommand(kioskId, any()) } returns mockCoordinateResponse

        // when
        val result = strategy.execute(context, uiList)

        // then
        assertTrue(result)

        verify(exactly = 1) { placeUtgService.initializeGraph(context, uiList) }

        verify(exactly = 1) { paymentAgent.determineAction(uiList) }
        verify(exactly = 1) { paymentNodeIntegrator.integratePaymentNode(mockAction, context) }
        verify(exactly = 1) { notificationService.sendActionCommand(kioskId, any()) }
    }

    @Test
    @DisplayName("execute: isPlaceDetermined가 true일 때, initializeGraph 미호출")
    fun `execute should not call initializeGraph when isPlaceDetermined is true`() {
        // given
        val kioskId = "kiosk-001"
        val context = mockk<UtgContext>()
        every { context.kioskId } returns kioskId
        every { context.isPlaceDetermined } returns true // ⭐️ 분기 2: true

        val uiList = listOf(mockk<UiComponentDto>())
        val mockAction = mockk<AgentActionDto>()
        val mockCoordinateResponse = mockk<CoordinateDto>()

        every { paymentAgent.determineAction(uiList) } returns mockAction
        every { mockAction.goNext } returns true
        every { mockAction.coordinate } returns listOf(10, 20)
        every { mockAction.title } returns "결제하기"

        every { paymentNodeIntegrator.integratePaymentNode(mockAction, context) } just runs
        every { notificationService.sendActionCommand(kioskId, any()) } returns mockCoordinateResponse

        // when
        val result = strategy.execute(context, uiList)

        // then
        assertTrue(result)

        verify(exactly = 0) { placeUtgService.initializeGraph(any(), any()) }

        verify(exactly = 1) { paymentAgent.determineAction(uiList) }
        verify(exactly = 1) { paymentNodeIntegrator.integratePaymentNode(mockAction, context) }
        verify(exactly = 1) { notificationService.sendActionCommand(kioskId, any()) }
    }

    @Test
    @DisplayName("execute: goNext가 false일 때, false 반환 (isPlaceDetermined=false 케이스)")
    fun `execute should return false when goNext is false (even if isPlaceDetermined is false)`() {
        // given
        val context = mockk<UtgContext>()
        every { context.kioskId } returns "kiosk-001"
        every { context.isPlaceDetermined } returns false

        val uiList = listOf(mockk<UiComponentDto>())
        val mockAction = mockk<AgentActionDto>()

        every { paymentAgent.determineAction(uiList) } returns mockAction
        every { mockAction.goNext } returns false

        every { placeUtgService.initializeGraph(context, uiList) } just runs

        // when
        val result = strategy.execute(context, uiList)

        // then
        assertFalse(result)

        verify(exactly = 1) { placeUtgService.initializeGraph(context, uiList) }
        verify(exactly = 1) { paymentAgent.determineAction(uiList) }

        verify(exactly = 0) { paymentNodeIntegrator.integratePaymentNode(any(), any()) }
        verify(exactly = 0) { notificationService.sendActionCommand(any(), any()) }
    }
}