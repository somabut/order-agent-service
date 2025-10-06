package com.orderagentservice.order.controller

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.logger
import com.orderagentservice.order.exception.KioskAdminSignInException
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.request.CategoryUtgUpdateRequest
import com.orderagentservice.order.model.request.MenuUtgUpdateRequest
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.OverlayType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.UtgService
import com.orderagentservice.order.service.utg.strategy.UtgActionFactory
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

    @PostMapping("/update/category/{kioskId}")
    fun updateCategoryUtg(
        @PathVariable kioskId: String,
        @RequestBody categoryUtgUpdateRequest: CategoryUtgUpdateRequest,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()

        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_START.title)
        val history = utgService.updateCategory(
            kioskId = kioskId, accessToken = accessToken,
            isInitPayment = categoryUtgUpdateRequest.initPayment
        )
        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_END.title)

        return ApiResponse.success(history)
    }

    @PostMapping("/update/menu/{kioskId}")
    fun updateMenuUtg(
        @PathVariable kioskId: String,
        @RequestBody menuUtgUpdateRequest: MenuUtgUpdateRequest,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()

        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_START.title)
        val history = utgService.updateMenu(
            kioskId = kioskId, accessToken = accessToken,
            isInitPayment = menuUtgUpdateRequest.initPayment
        )
        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_END.title)

        return ApiResponse.success(history)
    }

    @PostMapping("/update/payment/{kioskId}")
    fun updatePaymentUtg(
        @PathVariable kioskId: String,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()

        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_START.title)
        val history = utgService.updatePayment(kioskId, accessToken)
        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_END.title)

        return ApiResponse.success(history)
    }
}