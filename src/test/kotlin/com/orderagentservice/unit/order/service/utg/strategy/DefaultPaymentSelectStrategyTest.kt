package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.order.service.utg.payment.PaymentNodeIntegrator
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.strategy.DefaultPaymentSelectStrategy
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExtendWith(MockKExtension::class)
class DefaultPaymentSelectStrategyTest {
    @MockK
    private lateinit var paymentAgent: PaymentAgent
    @MockK
    private lateinit var paymentNodeIntegrator: PaymentNodeIntegrator
    @MockK
    private lateinit var notificationService: NotificationService

    private lateinit var strategy: DefaultPaymentSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = DefaultPaymentSelectStrategy(
            paymentAgent,
            paymentNodeIntegrator,
            notificationService
        )
    }

    @Test
    @DisplayName("execute: goNext가 true일 때, 노드 설정 및 true 반환")
    fun `execute should setup node and return true when goNext is true`() {
        // given
        val kioskId = "kiosk-001"
        val context = mockk<UtgContext>()
        every { context.kioskId } returns kioskId

        val uiList = listOf(mockk<UiComponentDto>())

        val mockAction = mockk<AgentActionDto>()
        val mockCoordinateResponse = mockk<CoordinateDto>()
        val coordinateSlot = slot<CoordinateDto>()

        every { paymentAgent.determineAction(uiList) } returns mockAction
        every { mockAction.goNext } returns true
        every { mockAction.coordinate } returns listOf(10, 20)
        every { mockAction.title } returns "결제하기"

        every { paymentNodeIntegrator.integratePaymentNode(mockAction, context) } just runs
        every { notificationService.sendActionCommand(kioskId, capture(coordinateSlot)) } returns mockCoordinateResponse

        // when
        val result = strategy.execute(context, uiList)

        // then
        assertTrue(result)
        verify(exactly = 1) { paymentAgent.determineAction(uiList) }
        verify(exactly = 1) { paymentNodeIntegrator.integratePaymentNode(mockAction, context) }
        verify(exactly = 1) { notificationService.sendActionCommand(kioskId, any()) }

        assertEquals(10, coordinateSlot.captured.x)
        assertEquals(20, coordinateSlot.captured.y)
    }

    @Test
    @DisplayName("execute: goNext가 false일 때, false 반환")
    fun `execute should return false when goNext is false`() {
        // given
        val context = mockk<UtgContext>(relaxed = true)
        val uiList = listOf(mockk<UiComponentDto>())
        val mockAction = mockk<AgentActionDto>()

        every { paymentAgent.determineAction(uiList) } returns mockAction
        every { mockAction.goNext } returns false

        // when
        val result = strategy.execute(context, uiList)

        // then
        assertFalse(result)
        verify(exactly = 1) { paymentAgent.determineAction(uiList) }

        verify(exactly = 0) { paymentNodeIntegrator.integratePaymentNode(any(), any()) }
        verify(exactly = 0) { notificationService.sendActionCommand(any(), any()) }
    }
}