package com.orderagentservice.order.controller

import com.orderagentservice.order.service.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/v1")
class SseController @Autowired constructor(
    private val notificationService: NotificationService
) {
    @GetMapping("/sse/connect", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connectSseLog(): SseEmitter {
        val emitter = notificationService.connectLog()
        return emitter
    }

    @GetMapping("/sse/connect/{kioskId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connectSseAction(@PathVariable kioskId: String): SseEmitter {
        val emitter = notificationService.connectAction(kioskId)
        return emitter
    }
}