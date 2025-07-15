package com.orderagentservice.order.controller

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.exception.KioskAdminSignInException
import com.orderagentservice.order.model.request.RandomTaskRequest
import com.orderagentservice.order.service.RandomTaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1")
class RandomTaskController @Autowired constructor(
    private val randomTaskService: RandomTaskService
) {
    @PostMapping("/order/test/{kioskId}")
    fun testOrder(
        @RequestBody randomTaskRequest: RandomTaskRequest,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()
        val result = randomTaskService.generate(randomTaskRequest.count, randomTaskRequest.kioskId, accessToken)
        return ApiResponse.success(result)
    }
}