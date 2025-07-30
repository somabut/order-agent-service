package com.orderagentservice.order.controller

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.model.request.ActionReplyRequest
import com.orderagentservice.order.model.response.CommandResponse
import com.orderagentservice.global.service.AmazonS3Service
import com.orderagentservice.order.model.dto.CoordinateDto
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
        val tempFile = File.createTempFile("capture_", image.originalFilename)
        image.transferTo(tempFile)

        notificationService.registerCaptureCommand(commandId, tempFile)
        amazonS3Service.saveFile(kioskId, commandId, tempFile)
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
                title = actionReplyRequest.changes!!.details[0].item.name
            )
        )
        return ApiResponse.success(CommandResponse(commandId))
    }
}