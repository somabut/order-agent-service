package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.menu.UiNodeIntegrator
import com.orderagentservice.order.service.utg.strategy.NoneBackSelectStrategy
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class NoneBackSelectStrategyTest {

    @MockK private lateinit var notificationService: NotificationService
    @MockK private lateinit var uiNodeIntegrator: UiNodeIntegrator
    @MockK private lateinit var screenNodeIntegrator: ScreenNodeIntegrator
    @MockK private lateinit var comparatorManager: ComparatorManager
    @MockK private lateinit var backAgent: BackAgent
    @MockK private lateinit var logService: LogService

    private lateinit var strategy: NoneBackSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = NoneBackSelectStrategy(
            notificationService,
            uiNodeIntegrator,
            screenNodeIntegrator,
            comparatorManager,
            backAgent,
            logService
        )
    }

    @Test
    @DisplayName("execute: hasOption 값과 관계없이 항상 빈 문자열을 반환")
    fun `execute should always return empty string regardless of hasOption`() {
        // given
        val context = mockk<UtgContext>()
        val menuNodeId = "menu-123"
        val uiList = listOf(mockk<UiComponentDto>())

        val resultTrue = strategy.execute(context, menuNodeId, uiList, hasOption = true)

        val resultFalse = strategy.execute(context, menuNodeId, uiList, hasOption = false)

        // then
        assertEquals("", resultTrue)
        assertEquals("", resultFalse)

        confirmVerified(
            notificationService,
            uiNodeIntegrator,
            screenNodeIntegrator,
            comparatorManager,
            backAgent,
            logService
        )
    }
}