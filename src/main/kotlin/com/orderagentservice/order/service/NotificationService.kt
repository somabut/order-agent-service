package com.orderagentservice.order.service

import com.orderagentservice.global.util.Sleeper
import com.orderagentservice.global.service.AmazonS3Service
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import com.orderagentservice.order.model.type.CommandType
import com.orderagentservice.order.exception.CommandTimeoutException
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.request.CommandRequest
import com.orderagentservice.order.repository.NotificationRepository
import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.global.util.Timer
import com.orderagentservice.order.model.dto.KioskCaptureDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@Service
class NotificationService @Autowired constructor(
    private val notificationRepository: NotificationRepository,
    private val amazonS3Service: AmazonS3Service,
    private val globalLogger: GlobalLogger,
    private val sleeper: Sleeper,
    private val timer: Timer,
) {
    private val log = logger()

//    val CONNECT_TIMEOUT: Long = 60L * 1000 * 60
    val CONNECT_TIMEOUT: Long = Long.MAX_VALUE
    val CAPTURE_WAIT_TIMEOUT: Long = 12_000
    val ACTION_WAIT_TIMEOUT: Long = 10_000
    val OVERLAY_WAIT_TIMEOUT: Long = 9_000

    fun connectAction(kioskId: String): SseEmitter {
        val emitter = notificationRepository.saveEmitter(kioskId, SseEmitter(CONNECT_TIMEOUT))
        log.info("액션 SSE 연결성공: ${kioskId}")
        val request = jsonMapper.writeValueAsString(
            CommandRequest(
                kioskId = kioskId,
                commandId = "connect",
                commandType = CommandType.CONNECT,
                data = "[order agent service]: 연결 성공"
            )
        )
        emitter.send(request)

        return emitter
    }

    fun registerCaptureCommand(kioskId: String, commandId: String, content: ByteArray, imageType: String) {
        val fileName = amazonS3Service.saveFile(
            kioskId = kioskId, commandId = commandId,
            fileBytes = content, contentType = imageType
        )
        val captureDto = KioskCaptureDto(content = content, type = imageType, url = fileName)
        notificationRepository.saveCaptureCommand(commandId, captureDto)
    }

    fun registerActionCommand(commandId: String, coordinate: CoordinateDto) {
        notificationRepository.saveActionCommand(commandId, coordinate)
    }

    fun registerOverlayCommand(commandId: String, overlay: String) {
        notificationRepository.saveOverLayCommand(commandId, overlay)
    }

    fun sendMessage(kioskId: String, message: String) {
        val emitter = notificationRepository.getEmitter(kioskId)
        log.info("액션 메시지 전송 -> [${kioskId}]")
        emitter.send(message)
    }

    fun sendCaptureCommand(kioskId: String): KioskCaptureDto {
        log.info("클라이언트에게 캡쳐 요청을 보냅니다.")
        val commandId = UUID.randomUUID().toString()
        val request = jsonMapper.writeValueAsString(
            CommandRequest(
            kioskId = kioskId,
            commandId = commandId,
            commandType = CommandType.CAPTURE,
            data = null
        ))
        val emitter = notificationRepository.getEmitter(kioskId)

        //클라이언트는 여기서 보내진 commandId로 응답을 해야함
        emitter.send(request)
        val captureDto = waitCaptureCommand(commandId)
        return captureDto
    }

    fun sendActionCommand(kioskId: String, coordinate: CoordinateDto): CoordinateDto {
        log.info("클라이언트에게 액션 요청을 보냅니다.")
        val commandId = UUID.randomUUID().toString()
        val request = jsonMapper.writeValueAsString(
            CommandRequest(
            kioskId = kioskId,
            commandId = commandId,
            commandType = CommandType.CLICK,
            data = coordinate
        ))
        val emitter = notificationRepository.getEmitter(kioskId)

        //클라이언트는 여기서 보내진 commandId로 응답을 해야함
        emitter.send(request)
        val coordinatePair: CoordinateDto
        try {
            coordinatePair = waitActionCommand(commandId)
        } catch (e: CommandTimeoutException) {
            globalLogger.loggingActionResult(kioskId, commandId, "CLICK", false, coordinate)
            throw e
        }

        //페이지 이동 시간을 고려해 잠시 대기
        sleeper.sleep(500)

        globalLogger.loggingActionResult(kioskId, commandId, "CLICK", true, coordinate)
        return coordinatePair
    }

    fun sendOverlayCommand(kioskId: String, overlay: String): String {
        log.info("클라이언트에게 오버레이를 요청합니다.")
        val commandId = UUID.randomUUID().toString()
        val request = jsonMapper.writeValueAsString(
            CommandRequest(
                kioskId = kioskId,
                commandId = commandId,
                commandType = CommandType.OVERLAY,
                data = overlay
            ))
        val emitter = notificationRepository.getEmitter(kioskId)

        emitter.send(request)
        val result = waitOverlayCommand(commandId)
        return result
    }

    fun sendCheckCommand(kioskId: String) {
        val commandId = UUID.randomUUID().toString()
        val request = jsonMapper.writeValueAsString(
            CommandRequest(
                kioskId = kioskId,
                commandId = commandId,
                commandType = CommandType.CHECK,
                data = null
            )
        )
        val emitter = notificationRepository.getEmitter(kioskId)

        emitter.send(request)
    }

    // 캡처 명령 대기
    private fun waitCaptureCommand(commandId: String): KioskCaptureDto {
        return waitForCommand(commandId, CAPTURE_WAIT_TIMEOUT) { id ->
            notificationRepository.removeCaptureCommand(id)
        }
    }

    // 액션 명령 대기
    private fun waitActionCommand(commandId: String): CoordinateDto {
        return waitForCommand(commandId, ACTION_WAIT_TIMEOUT) { id ->
            notificationRepository.removeActionCommand(id)
        }
    }

    private fun waitOverlayCommand(commandId: String): String {
        return waitForCommand(commandId, OVERLAY_WAIT_TIMEOUT) { id ->
            notificationRepository.removeOverLayCommand(id)
        }
    }

    private fun <T> waitForCommand(
        commandId: String,
        timeout: Long,
        commandRemover: (String) -> T?
    ): T {
        val startTime = timer.getCurrentTimeMillis()
        while (timer.getCurrentTimeMillis() - startTime < timeout) {
            val result = commandRemover(commandId)
            if (result != null) {
                return result
            }
            sleeper.sleep(200)
        }
        throw CommandTimeoutException()
    }
}