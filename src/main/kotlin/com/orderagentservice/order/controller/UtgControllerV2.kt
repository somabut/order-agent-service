package com.orderagentservice.order.controller

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.logger
import com.orderagentservice.order.exception.KioskAdminSignInException
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.OverlayType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.UtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v2/utg")
class UtgControllerV2 @Autowired constructor(
    private val notificationService: NotificationService,
    private val usageTracker: UsageTracker,
    private val utgService: UtgService
) {
    private val log = logger()

    @PostMapping("/init/{kioskId}")
    fun initializeUtg(
        @PathVariable kioskId: String,
        @RequestBody utgStrategyRequest: UtgStrategyRequest,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()

        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_START.title)
        val history = utgService.init(kioskId, accessToken, utgStrategyRequest)
        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_END.title)

        log.info("전체 토큰 사용량: ${usageTracker.totalUsage}")
        return ApiResponse.success(history)
    }
}