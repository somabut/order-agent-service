package com.orderagentservice.order.controller

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.order.model.response.AutoOrderResponse
import com.orderagentservice.order.service.auto.AutoOrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class OrderController @Autowired constructor(
    private val autoOrderService: AutoOrderService
) {
    @PostMapping("/order/start/{kioskId}/{taskId}")
    fun startOrder(@PathVariable kioskId: String, @PathVariable taskId: String, @RequestBody orderRequest: AutoOrderRequest): ApiResponse<*> {
        val history = autoOrderService.order(kioskId, taskId, orderRequest)

        return ApiResponse.success(
            AutoOrderResponse(
                taskId = taskId,
                history = history
            )
        )
    }
}