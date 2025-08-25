package com.orderagentservice.order.controller

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.logger
import com.orderagentservice.order.exception.KioskAdminSignInException
import com.orderagentservice.order.model.OverlayType
import com.orderagentservice.order.model.request.UtgUpdateRequest
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.auto.RandomTaskService
import com.orderagentservice.order.service.utg.UtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1")
class UtgController @Autowired constructor(
    private val utgService: UtgService,
    private val randomTaskService: RandomTaskService,
    private val usageTracker: UsageTracker,
    private val notificationService: NotificationService
) {
    private val log = logger()

    @GetMapping("/utg/init/{kioskId}")
    fun initializeUtg(
        @PathVariable kioskId: String,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()
        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG.title)

        val history = utgService.initializeGraph(kioskId, accessToken)
        log.info("전체 토큰 사용량: ${usageTracker.totalUsage}")
        return ApiResponse.success(history)
    }

    @PostMapping("/utg/update/{kioskId}")
    fun updateUtg(
        @PathVariable kioskId: String,
        @RequestHeader("Authorization", required = false) accessToken: String?,
        @RequestBody utgUpdateRequest: UtgUpdateRequest
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()

        val history = utgService.updateGraph(kioskId, utgUpdateRequest.editCategories, accessToken)
        log.info("전체 토큰 사용량: ${usageTracker.totalUsage}")
        return ApiResponse.success(history)
    }

    @GetMapping("/utg/benchmark/{kioskId}")
    fun updateUtg(
        @PathVariable kioskId: String,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()

        val result = randomTaskService.proceedUtg(kioskId, accessToken)
        log.info("전체 토큰 사용량: ${usageTracker.totalUsage}")
        return ApiResponse.success(result)
    }
}