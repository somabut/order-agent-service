package com.orderagentservice.order.controller

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.order.model.response.AutoOrderResponse
import com.orderagentservice.order.service.AutoOrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1")
class OrderController @Autowired constructor(
    private val autoOrderService: AutoOrderService
) {
    @PostMapping("/order/start/{kioskId}")
    fun startOrder(@PathVariable kioskId: String, @RequestBody orderRequest: AutoOrderRequest): ApiResponse<*> {
        val taskId = UUID.randomUUID().toString()
        autoOrderService.proceed(kioskId, taskId, orderRequest)
        var count = 0
        orderRequest.autoOrderMenus.forEach { count += it.count }
        return ApiResponse.success(
            AutoOrderResponse(
                taskId = taskId,
                menuCount = count,
                payment = orderRequest.payment
            )
        )
    }
}