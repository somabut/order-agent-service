package com.orderagentservice.order.util

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.logger
import com.orderagentservice.order.exception.UiExtractException
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
import java.io.FileInputStream
import java.nio.file.Files
import java.util.*
import kotlin.collections.HashMap


@Component
class UiExtractorManager @Autowired constructor(
    private val env: Environment
) {
    private val log = logger()

    private val UI_EXCTRACTOR_HOST = env.getProperty("ui-extractor.host")
    private val UI_EXCTRACTOR_PORT = env.getProperty("ui-extractor.port")
    private val UI_EXTRACTOR_API_KEY = env.getProperty("ui-extractor.api-key")!!

    fun queryUiExtractor(image: File): List<OmniUiComponentDto> {
        val restTemplate = RestTemplate()
        val url = "$UI_EXCTRACTOR_HOST/api/extract-ui"

        val fileContent = FileSystemResource(image)

        val body = LinkedMultiValueMap<String, Any>()
        body["file"] = fileContent

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers.setBearerAuth(UI_EXTRACTOR_API_KEY)

        //ui extractor service에게 UI 추출 요청
        try {
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
        } catch (e: RuntimeException) {
            throw UiExtractException()
        }
    }

    fun getUiComponents(image: File, kioskId: String): MutableList<LlmUiComponentDto> {
        //ui extractor에게 이미지 파싱 요청
        val uiComponents = queryUiExtractor(image)

        //옴니파서에게 받은 이미지 적절히 변환
        val llmUiList = mutableListOf<LlmUiComponentDto>()
        for (ele in uiComponents) {
            val width = ele.bbox.width
            val height = ele.bbox.height
            var title = ""
            for (str in ele.contents) {
                title += str
            }

            val pixelCoordinate = ele.bbox.coordinate.toPixel(width, height)
            val cord = pixelCoordinate.getCenter()
            llmUiList.add(
                LlmUiComponentDto(
                x = cord.first,
                y = cord.second,
                title = title
            ))
        }
        return llmUiList
    }
}