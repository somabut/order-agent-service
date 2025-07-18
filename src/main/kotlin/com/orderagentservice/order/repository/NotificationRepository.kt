package com.orderagentservice.order.repository

import com.orderagentservice.order.exception.NoSuchKioskException
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@Repository
class NotificationRepository {
    private val kioskNotification = ConcurrentHashMap<String, SseEmitter>()
    private val captureCommandCompleteMap = ConcurrentHashMap<String, File>()
    private val actionCommandCompleteMap = ConcurrentHashMap<String, Pair<Int, Int>>()
    private val onlySseEmitter = SseEmitter(60L * 1000 * 60)

    fun getLogEmitter(): SseEmitter = onlySseEmitter

    fun saveEmitter(kioskId: String, sseEmitter: SseEmitter): SseEmitter {
        kioskNotification[kioskId] = sseEmitter
        return sseEmitter
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

    fun saveActionCommand(commandId: String, coordinate: Pair<Int, Int>) {
        actionCommandCompleteMap[commandId] = coordinate
    }

    fun removeActionCommand(commandId: String): Pair<Int, Int>? = actionCommandCompleteMap.remove(commandId)
}