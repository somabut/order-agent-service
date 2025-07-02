package com.orderagentservice.order.util

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.model.dto.OmniUiComponentDto
import com.orderagentservice.order.model.response.OmniResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.File

@Component
class UiExtractorManager @Autowired constructor(
    private val env: Environment
) {
    private val UI_EXCTRACTOR_HOST = env.getProperty("ui-extractor.url")
    private val UI_EXCTRACTOR_PORT = env.getProperty("ui-extractor.port")

    fun queryUiExtractor(image: File): List<OmniUiComponentDto> {
        val restTemplate = RestTemplate()
        val url = "$UI_EXCTRACTOR_HOST:$UI_EXCTRACTOR_PORT/api/extract-ui"

        val fileResource = FileSystemResource(image)
        val body = LinkedMultiValueMap<String, Any>()
        body.add("file", fileResource)

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        //ui extractor service에게 UI 추출 요청
        val requestEntity = HttpEntity(body, headers)
        val responseEntity = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            object : ParameterizedTypeReference<ApiResponse<OmniResponse>>() {}
        )
        val response: ApiResponse<OmniResponse> = responseEntity.body!!
        val uiComponents = response.data!!.uiComponents
        return uiComponents
    }
}