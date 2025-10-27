package com.orderagentservice.unit.order.service

import com.orderagentservice.order.repository.NotificationRepository
import com.orderagentservice.order.service.HeartBeatScheduler
import com.orderagentservice.order.service.NotificationService
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible

@ExtendWith(MockKExtension::class)
class HeartBeatSchedulerTest {
    @MockK
    private lateinit var notificationService: NotificationService

    @MockK
    private lateinit var notificationRepository: NotificationRepository

    private lateinit var scheduler: HeartBeatScheduler

    private val MOCK_KIOSK_A = "kiosk-A"
    private val MOCK_KIOSK_B = "kiosk-B"

    @MockK(relaxed = true)
    private lateinit var mockEmitterA: SseEmitter

    @MockK(relaxed = true)
    private lateinit var mockEmitterB: SseEmitter

    @BeforeEach
    fun setUp() {
        scheduler = spyk(
            HeartBeatScheduler(notificationService, notificationRepository),
            recordPrivateCalls = true
        )
    }

    @Test
    @DisplayName("checkConnections: 모든 Emitter가 정상일 때, sendCheckCommand만 호출한다")
    fun `checkConnections should just send commands when all emitters are healthy`() {
        // given
        val emittersMap = mapOf(MOCK_KIOSK_A to mockEmitterA, MOCK_KIOSK_B to mockEmitterB)
        every { notificationRepository.getAllEmitter() } returns emittersMap
        every { notificationService.sendCheckCommand(any()) } just Runs

        val checkConnectionsMethod = scheduler::class.declaredFunctions
            .find { it.name == "checkConnections" }!!
        checkConnectionsMethod.isAccessible = true

        // when
        checkConnectionsMethod.call(scheduler)

        // then
        verify(exactly = 1) { notificationService.sendCheckCommand(MOCK_KIOSK_A) }
        verify(exactly = 1) { notificationService.sendCheckCommand(MOCK_KIOSK_B) }

        verify(exactly = 0) { notificationRepository.getEmitter(any()) }
        verify(exactly = 0) { notificationRepository.deleteByKioskId(any()) }
    }

    @Test
    @DisplayName("checkConnections: Kiosk-B의 sendCheckCommand 실패 시, deleteByKioskId를 호출한다")
    fun `checkConnections should delete emitter when sendCheckCommand fails`() {
        // given
        val emittersMap = mapOf(MOCK_KIOSK_A to mockEmitterA, MOCK_KIOSK_B to mockEmitterB)
        every { notificationRepository.getAllEmitter() } returns emittersMap

        every { notificationService.sendCheckCommand(MOCK_KIOSK_A) } just Runs
        every { notificationService.sendCheckCommand(MOCK_KIOSK_B) } throws Exception("Connection lost")

        every { notificationRepository.getEmitter(MOCK_KIOSK_B) } returns mockEmitterB
        every { notificationRepository.deleteByKioskId(MOCK_KIOSK_B) } just Runs

        val checkConnectionsMethod = scheduler::class.declaredFunctions
            .find { it.name == "checkConnections" }!!
        checkConnectionsMethod.isAccessible = true

        // when
        checkConnectionsMethod.call(scheduler)

        // then
        verify(exactly = 1) { notificationService.sendCheckCommand(MOCK_KIOSK_A) }
        verify(exactly = 1) { notificationService.sendCheckCommand(MOCK_KIOSK_B) }
        verify(exactly = 1) { notificationRepository.getEmitter(MOCK_KIOSK_B) }
        verify(exactly = 1) { notificationRepository.deleteByKioskId(MOCK_KIOSK_B) }
        verify(exactly = 0) { notificationRepository.deleteByKioskId(MOCK_KIOSK_A) }
    }

    @Test
    @DisplayName("checkConnections: Kiosk-B가 이미 삭제된 경우(send 실패, get 실패), delete를 호출하지 않는다")
    fun `checkConnections should not delete if emitter is already gone`() {
        // given
        val emittersMap = mapOf(MOCK_KIOSK_A to mockEmitterA, MOCK_KIOSK_B to mockEmitterB)
        every { notificationRepository.getAllEmitter() } returns emittersMap

        every { notificationService.sendCheckCommand(MOCK_KIOSK_A) } just Runs
        every { notificationService.sendCheckCommand(MOCK_KIOSK_B) } throws Exception("Connection lost")
        every { notificationRepository.getEmitter(MOCK_KIOSK_B) } throws Exception("Not found")
        every { notificationRepository.deleteByKioskId(any()) } just Runs

        val checkConnectionsMethod = scheduler::class.declaredFunctions
            .find { it.name == "checkConnections" }!!
        checkConnectionsMethod.isAccessible = true

        // when
        checkConnectionsMethod.call(scheduler)

        // then
        verify(exactly = 1) { notificationService.sendCheckCommand(MOCK_KIOSK_A) }
        verify(exactly = 1) { notificationService.sendCheckCommand(MOCK_KIOSK_B) }

        verify(exactly = 1) { notificationRepository.getEmitter(MOCK_KIOSK_B) }

        verify(exactly = 0) { notificationRepository.deleteByKioskId(any()) }
    }
}