package com.orderagentservice.unit.order.service

import com.orderagentservice.global.util.Sleeper
import com.orderagentservice.global.service.AmazonS3Service
import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.global.util.Timer
import com.orderagentservice.order.exception.CommandTimeoutException
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.KioskCaptureDto
import com.orderagentservice.order.repository.NotificationRepository
import com.orderagentservice.order.service.NotificationService
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@ExtendWith(MockKExtension::class)
class NotificationServiceTest {
    @MockK
    private lateinit var notificationRepository: NotificationRepository
    @MockK
    private lateinit var amazonS3Service: AmazonS3Service
    @MockK
    private lateinit var sleeper: Sleeper
    @MockK
    private lateinit var timer: Timer
    @MockK
    private lateinit var globalLogger: GlobalLogger

    @MockK(relaxed = true) // send() 등 모든 호출을 무시
    private lateinit var mockEmitter: SseEmitter

    private lateinit var notificationService: NotificationService

    private val MOCK_KIOSK_ID = "kiosk-123"
    private val MOCK_COMMAND_ID = "cmd-uuid-abc"
    private val MOCK_MESSAGE = "test"
    private val MOCK_BYTES = byteArrayOf(1, 2, 3)
    private val MOCK_IMAGE_TYPE = "image/png"
    private val MOCK_FILE_URL = "https://s3.aws.com/image.png"
    private val MOCK_COORDINATE_DTO = CoordinateDto(x = 100, y = 200, title = "TEST")
    private val MOCK_OVERLAY_STRING = "overlay_data"
    private val MOCK_CAPTURE_DTO = KioskCaptureDto(
        content = MOCK_BYTES,
        type = MOCK_IMAGE_TYPE,
        url = MOCK_FILE_URL
    )

    @BeforeEach
    fun setUp() {
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns MOCK_COMMAND_ID


        mockkConstructor(SseEmitter::class)
        every {
            notificationRepository.saveEmitter(
                any<String>(),
                any<SseEmitter>()
            )
        } returns mockEmitter // 우리가 만든 가짜 emitter를 반환

        notificationService = NotificationService(
            notificationRepository,
            amazonS3Service,
            globalLogger,
            sleeper,
            timer,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @DisplayName("connectAction: 새 Emitter를 생성, 저장하고 연결 성공 메시지를 보낸다")
    fun `connectAction should save new emitter and send connect message`() {
        // given
        every { notificationRepository.saveEmitter(MOCK_KIOSK_ID, mockEmitter) } returns mockEmitter

        // jsonMapper가 생성할 JSON을 캡처할 slot
        val jsonSlot = slot<String>()
        every { mockEmitter.send(capture(jsonSlot)) } just Runs

        // when
        val result = notificationService.connectAction(MOCK_KIOSK_ID)

        // then
        assertThat(result).isSameAs(mockEmitter)
        verify(exactly = 1) { notificationRepository.saveEmitter(eq(MOCK_KIOSK_ID), any<SseEmitter>()) }
        verify { mockEmitter.send(any<String>()) }
        assertThat(jsonSlot.captured).contains("\"kioskId\":\"$MOCK_KIOSK_ID\"")
        assertThat(jsonSlot.captured).contains("\"commandId\":\"connect\"")
        assertThat(jsonSlot.captured).contains("\"commandType\":\"CONNECT\"")
        assertThat(jsonSlot.captured).contains("\"data\":\"[order agent service]: 연결 성공\"")
    }

    @Test
    @DisplayName("registerCaptureCommand: S3에 파일 저장 후 DTO를 레포지토리에 저장한다")
    fun `registerCaptureCommand should save file to S3 and save DTO to repository`() {
        // given
        every {
            amazonS3Service.saveFile(
                kioskId = MOCK_KIOSK_ID,
                commandId = MOCK_COMMAND_ID,
                fileBytes = MOCK_BYTES,
                contentType = MOCK_IMAGE_TYPE
            )
        } returns MOCK_FILE_URL

        val dtoSlot = slot<KioskCaptureDto>()
        every { notificationRepository.saveCaptureCommand(MOCK_COMMAND_ID, capture(dtoSlot)) } just Runs
        every { sleeper.sleep(any<Long>()) } just Runs
//        every { timer.getCurrentTimeMillis() } returns System.currentTimeMillis()
        // when
        notificationService.registerCaptureCommand(MOCK_KIOSK_ID, MOCK_COMMAND_ID, MOCK_BYTES, MOCK_IMAGE_TYPE)

        // then
        verify { amazonS3Service.saveFile(MOCK_KIOSK_ID, MOCK_COMMAND_ID, MOCK_BYTES, MOCK_IMAGE_TYPE) }
        verify { notificationRepository.saveCaptureCommand(MOCK_COMMAND_ID, any()) }
        assertThat(dtoSlot.captured.url).isEqualTo(MOCK_FILE_URL)
        assertThat(dtoSlot.captured.content).isEqualTo(MOCK_BYTES)
    }

    @Test
    @DisplayName("registerActionCommand: DTO를 레포지토리에 저장한다")
    fun `registerActionCommand should save coordinate DTO to repository`() {
        // given
        every { notificationRepository.saveActionCommand(MOCK_COMMAND_ID, MOCK_COORDINATE_DTO) } just Runs
        every { sleeper.sleep(any<Long>()) } just Runs

        // when
        notificationService.registerActionCommand(MOCK_COMMAND_ID, MOCK_COORDINATE_DTO)

        // then
        verify { notificationRepository.saveActionCommand(MOCK_COMMAND_ID, MOCK_COORDINATE_DTO) }
    }

    @Test
    @DisplayName("registerOverlayCommand: 오버레이 문자열을 레포지토리에 저장한다")
    fun `registerOverlayCommand should save overlay string to repository`() {
        // given
        every { notificationRepository.saveOverLayCommand(MOCK_COMMAND_ID, MOCK_OVERLAY_STRING) } just Runs
        every { sleeper.sleep(any<Long>()) } just Runs

        // when
        notificationService.registerOverlayCommand(MOCK_COMMAND_ID, MOCK_OVERLAY_STRING)

        // then
        verify { notificationRepository.saveOverLayCommand(MOCK_COMMAND_ID, MOCK_OVERLAY_STRING) }
    }

    @Test
    @DisplayName("sendMessage: 특정 Kiosk ID의 Emitter에 메시지를 전송한다")
    fun `sendMessage should send message to specific emitter`() {
        // given
        every { notificationRepository.getEmitter(MOCK_KIOSK_ID) } returns mockEmitter

        // when
        notificationService.sendMessage(MOCK_KIOSK_ID, MOCK_MESSAGE)

        // then
        verify { notificationRepository.getEmitter(MOCK_KIOSK_ID) }
        verify { mockEmitter.send(MOCK_MESSAGE) }
    }

    @Test
    @DisplayName("sendCaptureCommand (성공): 캡처 명령 전송 후, 레포지토리에서 DTO를 성공적으로 받아온다")
    fun `sendCaptureCommand should wait and return DTO on success`() {
        // given
        every { notificationRepository.getEmitter(MOCK_KIOSK_ID) } returns mockEmitter
        every { timer.getCurrentTimeMillis() } returns 1000L
        every { notificationRepository.removeCaptureCommand(MOCK_COMMAND_ID) } returns MOCK_CAPTURE_DTO

        val jsonSlot = slot<String>()
        every { mockEmitter.send(capture(jsonSlot)) } just Runs

        // when
        val result = notificationService.sendCaptureCommand(MOCK_KIOSK_ID)

        // then
        assertThat(result).isSameAs(MOCK_CAPTURE_DTO)
        verify { mockEmitter.send(any<String>()) }

        assertThat(jsonSlot.captured).contains("\"commandId\":\"$MOCK_COMMAND_ID\"")
        assertThat(jsonSlot.captured).contains("\"commandType\":\"CAPTURE\"")
        assertThat(jsonSlot.captured).contains("\"data\":null")

        verify(exactly = 1) { notificationRepository.removeCaptureCommand(MOCK_COMMAND_ID) }
    }

    @Test
    @DisplayName("sendCaptureCommand (타임아웃): 캡처 명령 전송 후, DTO를 받아오지 못하면 CommandTimeoutException 발생")
    fun `sendCaptureCommand should throw CommandTimeoutException on timeout`() {
        // given
        every { notificationRepository.getEmitter(MOCK_KIOSK_ID) } returns mockEmitter
        every { timer.getCurrentTimeMillis() } returnsMany listOf(
            1000L,
            1000L + notificationService.CAPTURE_WAIT_TIMEOUT + 1L
        )
        every { notificationRepository.removeCaptureCommand(MOCK_COMMAND_ID) } returns null

        // when & then
        assertThrows<CommandTimeoutException> {
            notificationService.sendCaptureCommand(MOCK_KIOSK_ID)
        }

        verify { mockEmitter.send(any<String>()) }
//        verify(atLeast = 1) { notificationRepository.removeCaptureCommand(MOCK_COMMAND_ID) }
    }

    @Test
    @DisplayName("sendActionCommand (성공): 액션 명령 전송 후, DTO를 받고, 로그를 남기고, 500ms 대기한다")
    fun `sendActionCommand should wait, log success, and sleep 500ms`() {
        // given
        every { notificationRepository.getEmitter(MOCK_KIOSK_ID) } returns mockEmitter
        every { sleeper.sleep(any<Long>()) } just Runs
        every { timer.getCurrentTimeMillis() } returns 1000L
        every { notificationRepository.removeActionCommand(MOCK_COMMAND_ID) } returns MOCK_COORDINATE_DTO
        every { globalLogger.loggingActionResult(any(), any(), any(), any(), any()) } just Runs

        val jsonSlot = slot<String>()
        every { mockEmitter.send(capture(jsonSlot)) } just Runs

        // when
        val result = notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORDINATE_DTO)

        // then
        assertThat(result).isSameAs(MOCK_COORDINATE_DTO)

        verify { mockEmitter.send(any<String>()) }
        assertThat(jsonSlot.captured).contains("\"commandId\":\"$MOCK_COMMAND_ID\"")
        assertThat(jsonSlot.captured).contains("\"commandType\":\"CLICK\"")
        assertThat(jsonSlot.captured)
            .contains("{\"kioskId\":\"kiosk-123\",\"commandId\":\"cmd-uuid-abc\",\"commandType\":\"CLICK\",\"data\":{\"x\":100,\"y\":200,\"title\":\"TEST\"}}");

        verify(exactly = 1) { notificationRepository.removeActionCommand(MOCK_COMMAND_ID) }
        verify(exactly = 1) {
            globalLogger.loggingActionResult(MOCK_KIOSK_ID, MOCK_COMMAND_ID, "CLICK", true, MOCK_COORDINATE_DTO)
        }
        verify(exactly = 1) { sleeper.sleep(500L) }
    }

    @Test
    @DisplayName("sendActionCommand (타임아웃): 타임아웃 발생 시, 실패 로그를 남기고, 대기(sleep) 없이 예외를 던진다")
    fun `sendActionCommand should log failure and throw exception on timeout`() {
        // given
        every { notificationRepository.getEmitter(MOCK_KIOSK_ID) } returns mockEmitter
        every { timer.getCurrentTimeMillis() } returnsMany listOf(
            1000L,
            1000L + notificationService.ACTION_WAIT_TIMEOUT + 1L
        )
        every { notificationRepository.removeActionCommand(MOCK_COMMAND_ID) } returns null
        every { globalLogger.loggingActionResult(any(), any(), any(), any(), any()) } just Runs

        // when & then
        assertThrows<CommandTimeoutException> {
            notificationService.sendActionCommand(MOCK_KIOSK_ID, MOCK_COORDINATE_DTO)
        }

        verify(exactly = 1) {
            globalLogger.loggingActionResult(MOCK_KIOSK_ID, MOCK_COMMAND_ID, "CLICK", false, MOCK_COORDINATE_DTO)
        }
        verify(exactly = 0) { sleeper.sleep(500L) }
    }

    @Test
    @DisplayName("sendOverlayCommand (성공): 오버레이 명령 전송 후, 문자열을 성공적으로 받아온다")
    fun `sendOverlayCommand should wait and return String on success`() {
        // given
        every { notificationRepository.getEmitter(MOCK_KIOSK_ID) } returns mockEmitter
        every { timer.getCurrentTimeMillis() } returns 1000L
        every { notificationRepository.removeOverLayCommand(MOCK_COMMAND_ID) } returns MOCK_OVERLAY_STRING

        val jsonSlot = slot<String>()
        every { mockEmitter.send(capture(jsonSlot)) } just Runs

        // when
        val result = notificationService.sendOverlayCommand(MOCK_KIOSK_ID, MOCK_OVERLAY_STRING)

        // then
        assertThat(result).isEqualTo(MOCK_OVERLAY_STRING)

        verify { mockEmitter.send(any<String>()) }
        assertThat(jsonSlot.captured).contains("\"commandId\":\"$MOCK_COMMAND_ID\"")
        assertThat(jsonSlot.captured).contains("\"commandType\":\"OVERLAY\"")
        assertThat(jsonSlot.captured).contains("\"data\":\"$MOCK_OVERLAY_STRING\"")

        verify(exactly = 1) { notificationRepository.removeOverLayCommand(MOCK_COMMAND_ID) }
    }

    @Test
    @DisplayName("sendCheckCommand: Emitter에 체크 명령을 전송한다 (응답 대기 없음)")
    fun `sendCheckCommand should send message without waiting`() {
        // given
        every { notificationRepository.getEmitter(MOCK_KIOSK_ID) } returns mockEmitter

        val jsonSlot = slot<String>()
        every { mockEmitter.send(capture(jsonSlot)) } just Runs

        // when
        notificationService.sendCheckCommand(MOCK_KIOSK_ID)

        // then
        verify { mockEmitter.send(any<String>()) }
        assertThat(jsonSlot.captured).contains("\"commandType\":\"CHECK\"")

        verify(exactly = 0) { notificationRepository.removeActionCommand(any()) }
        verify(exactly = 0) { notificationRepository.removeCaptureCommand(any()) }
        verify(exactly = 0) { notificationRepository.removeOverLayCommand(any()) }
    }
}