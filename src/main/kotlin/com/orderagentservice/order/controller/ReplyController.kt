package com.orderagentservice.order.controller

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.model.request.ActionReplyRequest
import com.orderagentservice.order.model.response.CommandResponse
import com.orderagentservice.global.service.AmazonS3Service
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.request.OverlayReplyRequest
import com.orderagentservice.order.service.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File

@RestController
@RequestMapping("/v1")
class ReplyController @Autowired constructor(
    private val notificationService: NotificationService,
    private val amazonS3Service: AmazonS3Service
) {
    @PostMapping("/command/capture/{kioskId}/{commandId}")
    fun replyCapture(
        @PathVariable kioskId: String,
        @PathVariable commandId: String,
        @RequestParam image: MultipartFile
    ): ApiResponse<*> {
        val fileBytes = image.bytes
        val tempFile = File.createTempFile("capture_", image.originalFilename)
        tempFile.writeBytes(fileBytes)

        try {
            notificationService.registerCaptureCommand(kioskId, commandId, tempFile)
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
        return ApiResponse.success(CommandResponse(commandId))
    }

    @PostMapping("/command/action/{kioskId}/{commandId}")
    fun replyAction(
        @PathVariable kioskId: String,
        @PathVariable commandId: String,
        @RequestBody actionReplyRequest: ActionReplyRequest
    ): ApiResponse<*> {
        notificationService.registerActionCommand(
            commandId,
            CoordinateDto(
                x = actionReplyRequest.x,
                y = actionReplyRequest.y,
                title = ""
            )
        )
        return ApiResponse.success(CommandResponse(commandId))
    }

    @PostMapping("/command/overlay/{kioskId}/{commandId}")
    fun replyOverlay(
        @PathVariable kioskId: String,
        @PathVariable commandId: String,
        @RequestBody overlayReplyRequest: OverlayReplyRequest
    ): ApiResponse<*> {
        notificationService.registerOverlayCommand(
            commandId = commandId,
            overlay = overlayReplyRequest.overlay
        )
        return ApiResponse.success(CommandResponse(commandId))
    }
}