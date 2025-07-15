package com.orderagentservice.order.service

import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import com.orderagentservice.order.model.CommandType
import com.orderagentservice.order.exception.CommandTimeoutException
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.request.CommandRequest
import com.orderagentservice.order.repository.NotificationRepository
import com.orderagentservice.global.util.GlobalLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.File
import java.util.UUID

@Service
class NotificationService @Autowired constructor(
    private val notificationRepository: NotificationRepository,
    private val globalLogger: GlobalLogger
) {
    private val log = logger()

    val CONNECT_TIMEOUT: Long = 60L * 1000 * 60
    val CAPTURE_WAIT_TIMEOUT: Long = 10_000
    val ACTION_WAIT_TIMEOUT: Long = 4_000

    fun connect(kioskId: String): SseEmitter {
        val emitter = notificationRepository.saveEmitter(kioskId, SseEmitter(CONNECT_TIMEOUT))
        emitter.onTimeout { notificationRepository.deleteByKioskId(kioskId) }
        emitter.send("[order agent service]: 연결 성공")

        return emitter
    }

    fun registerCaptureCommand(commandId: String, image: File) {
        notificationRepository.saveCaptureCommand(commandId, image)
    }

    fun registerActionCommand(commandId: String, coordinate: Pair<Int, Int>) {
        notificationRepository.saveActionCommand(commandId, coordinate)
    }

    fun sendMessage(kioskId: String, message: String) {
        val emitter = notificationRepository.getEmitter(kioskId)
        emitter.send(message)
    }

    fun sendCaptureCommand(kioskId: String): File {
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
        val file = waitCaptureCommand(commandId)
        return file
    }

    fun sendActionCommand(kioskId: String, coordinate: CoordinateDto): Pair<Int, Int> {
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
        val coordinatePair: Pair<Int, Int>
        try {
            coordinatePair = waitActionCommand(commandId)
        } catch (e: CommandTimeoutException) {
            globalLogger.loggingActionResult(kioskId, commandId, "CLICK", false, coordinate)
            throw e
        }

        globalLogger.loggingActionResult(kioskId, commandId, "CLICK", true, coordinate)
        return coordinatePair
    }

    // 캡처 명령 대기
    private fun waitCaptureCommand(commandId: String): File {
        return waitForCommand(commandId, CAPTURE_WAIT_TIMEOUT) { id ->
            notificationRepository.removeCaptureCommand(id)
        }
    }

    // 액션 명령 대기
    private fun waitActionCommand(commandId: String): Pair<Int, Int> {
        return waitForCommand(commandId, ACTION_WAIT_TIMEOUT) { id ->
            notificationRepository.removeActionCommand(id)
        }
    }

    private fun <T> waitForCommand(
        commandId: String,
        timeout: Long,
        commandRemover: (String) -> T?
    ): T {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeout) {
            val result = commandRemover(commandId)
            if (result != null) {
                return result
            }
            Thread.sleep(200)
        }
        throw CommandTimeoutException()
    }

}