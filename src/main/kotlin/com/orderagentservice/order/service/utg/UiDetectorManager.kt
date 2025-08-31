package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.logger
import com.orderagentservice.order.exception.UiExtractException
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.DetectorUiComponentDto
import com.orderagentservice.order.model.response.DetectorResponse
import com.orderagentservice.order.service.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.core.io.ByteArrayResource
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
class UiDetectorManager @Autowired constructor(
    private val env: Environment,
    private val notificationService: NotificationService
) {
    private val log = logger()

    private val UI_EXCTRACTOR_HOST = env.getProperty("ui-extractor.host")
    private val UI_EXTRACTOR_API_KEY = env.getProperty("ui-extractor.api-key")!!

    fun queryUiExtractor(imageByte: ByteArray, endpoint: String): List<DetectorUiComponentDto> {
        val restTemplate = RestTemplate()
        val url = "$UI_EXCTRACTOR_HOST/v2/$endpoint"

        val fileContent = ByteArrayResource(imageByte)

        val body = LinkedMultiValueMap<String, Any>()
        body["file"] = fileContent

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers.setBearerAuth(UI_EXTRACTOR_API_KEY)

        //ui extractor service에게 UI 추출 요청
        var requestCount = 0
        val maxRequest = 4
        while (requestCount < maxRequest) {
            try {
                val requestEntity = HttpEntity(body, headers)
                val responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    object : ParameterizedTypeReference<ApiResponse<DetectorResponse>>() {}
                )
                val response: ApiResponse<DetectorResponse> = responseEntity.body!!
                val uiComponents = response.data!!.uiComponents
                return uiComponents
            }catch (e: RuntimeException) {
                log.error(e.message)
                log.info("UI extractor service요류로 인해 재시도합니다. 현재횟수: $requestCount")
            }
            requestCount++
        }
        throw UiExtractException()
    }

    fun getUiComponents(context: GraphContext, isPayment: Boolean = false): MutableList<UiComponentDto> {
        //ui extractor에게 이미지 파싱 요청
        val captureDto = notificationService.sendCaptureCommand(context.kioskId)
        val imageBytes = captureDto.content
        val uiComponents = queryUiExtractor(imageBytes, if (!isPayment) "extract-ui" else "ocr")
        context.imageName = captureDto.name

        //옴니파서에게 받은 이미지 적절히 변환
        val llmUiList = mutableListOf<UiComponentDto>()
        for (ele in uiComponents) {
            var title = ""
            for (str in ele.contents) {
                title += str
            }

            val pixelCoordinate = ele.bbox.coordinate
            val cord = pixelCoordinate.getCenter()
            llmUiList.add(
                UiComponentDto(
                x = cord.first,
                y = cord.second,
                title = title
            ))
        }
        return llmUiList
    }
}