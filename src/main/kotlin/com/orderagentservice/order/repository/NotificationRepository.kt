package com.orderagentservice.order.repository

import com.orderagentservice.order.exception.NoSuchKioskException
import com.orderagentservice.order.model.dto.CoordinateDto
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

@Repository
class NotificationRepository {
    private val kioskNotification = ConcurrentHashMap<String, SseEmitter>()
    private val captureCommandCompleteMap = ConcurrentHashMap<String, File>()
    private val actionCommandCompleteMap = ConcurrentHashMap<String, CoordinateDto>()
    private val logChannelSet = CopyOnWriteArraySet<SseEmitter>()

    fun getLogEmitters() = logChannelSet

    fun saveEmitter(kioskId: String, sseEmitter: SseEmitter): SseEmitter {
        kioskNotification[kioskId] = sseEmitter
        return sseEmitter
    }

    fun deleteLogEmitter(sseEmitter: SseEmitter) {
        logChannelSet.remove(sseEmitter)
    }

    fun deleteByKioskId(kioskId: String) {
        kioskNotification.remove(kioskId)
    }

    fun getEmitter(kioskId: String): SseEmitter =
        kioskNotification[kioskId] ?: throw NoSuchKioskException()

    fun saveCaptureCommand(commandId: String, file: File) {
        captureCommandCompleteMap[commandId] = file
    }

    fun removeCaptureCommand(commandId: String): File? = captureCommandCompleteMap.remove(commandId)

    fun saveActionCommand(commandId: String, coordinate: CoordinateDto) {
        actionCommandCompleteMap[commandId] = coordinate
    }

    fun removeActionCommand(commandId: String): CoordinateDto? = actionCommandCompleteMap.remove(commandId)
}