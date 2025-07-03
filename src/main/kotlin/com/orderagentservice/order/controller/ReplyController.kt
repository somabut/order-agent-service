package com.orderagentservice.order.controller

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.model.response.CommandResponse
import com.orderagentservice.order.service.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File

@RestController
class ReplyController @Autowired constructor(
    private val notificationService: NotificationService
) {
    @PostMapping("/api/command/capture/{commandId}")
    fun replyCapture(@PathVariable commandId: String, @RequestParam image: MultipartFile): ApiResponse<*> {
        val tempFile = File.createTempFile("capture_", image.originalFilename)
        image.transferTo(tempFile)

        notificationService.registerCaptureCommand(commandId, tempFile)
        return ApiResponse.success(CommandResponse(commandId))
    }

    @PostMapping("/api/command/action/{commandId}")
    fun replyAction(@PathVariable commandId: String, @RequestParam x: Int, @RequestParam y: Int): ApiResponse<*> {
        notificationService.registerActionCommand(commandId, Pair(x, y))
        return ApiResponse.success(CommandResponse(commandId))
    }
}