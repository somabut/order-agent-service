package com.orderagentservice.order.controller

import com.orderagentservice.order.service.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController @Autowired constructor(
    private val notificationService: NotificationService
) {
    @GetMapping("/")
    fun test(): String {
        return "ok"
    }

    @GetMapping("/test/{kioskId}")
    fun sendMessageTest(@PathVariable kioskId: String) {
        notificationService.sendMessage(kioskId, "test message from moodTRBL")
    }
}