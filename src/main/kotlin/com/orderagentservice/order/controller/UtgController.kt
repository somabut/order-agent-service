package com.orderagentservice.order.controller

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.exception.KioskAdminSignInException
import com.orderagentservice.order.service.GraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1")
class UtgController @Autowired constructor(
    private val graphService: GraphService
) {
    @GetMapping("/utg/init/{kioskId}")
    fun initializeUtg(
        @PathVariable kioskId: String,
        @RequestHeader("Authorization", required = false) accessToken: String?
    ): ApiResponse<*> {
        if (accessToken == null) throw KioskAdminSignInException()
        val history = graphService.initializeGraph(kioskId, accessToken)
        return ApiResponse.success(history)
    }
}