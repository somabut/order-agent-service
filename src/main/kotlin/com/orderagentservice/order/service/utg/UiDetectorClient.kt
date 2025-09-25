package com.orderagentservice.order.service.utg

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.model.response.DetectorResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@FeignClient(
    name = "ui-extractor",
    url = "\${ui-extractor.host}",
)
interface UiDetectorClient {
    @PostMapping("/v2/extract-ui", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun extractUi(
        @RequestPart("file") file: MultipartFile,
        @RequestHeader("Authorization") authorization: String
    ): ApiResponse<DetectorResponse>
}