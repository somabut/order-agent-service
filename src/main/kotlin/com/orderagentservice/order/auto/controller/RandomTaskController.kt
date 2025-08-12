package com.orderagentservice.order.auto.controller

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.exception.KioskAdminSignInException
import com.orderagentservice.order.auto.model.request.RandomTaskRequest
import com.orderagentservice.order.auto.service.RandomTaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1")
class RandomTaskController @Autowired constructor(
    private val randomTaskService: RandomTaskService
) {
    @PostMapping("/order/benchmark/{kioskId}")
    fun testOrder(
        @RequestBody randomTaskRequest: RandomTaskRequest,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()

        randomTaskService.proceedAutoOrder(
            count = randomTaskRequest.count,
            kioskId = randomTaskRequest.kioskId,
            accessToken = accessToken
        )
        return ApiResponse.success("")
    }
}