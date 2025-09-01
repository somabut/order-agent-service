package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.global.util.ImageUtils
import com.orderagentservice.logger
import com.orderagentservice.order.exception.UiExtractException
import com.orderagentservice.order.model.type.ExtractType
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.DetectorUiComponentDto
import com.orderagentservice.order.model.dto.KioskCaptureDto
import com.orderagentservice.order.model.dto.OcrDto
import com.orderagentservice.order.model.dto.ScreenDto
import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.model.dto.YoloDto
import com.orderagentservice.order.model.response.DetectorResponse
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.ocr.OcrGraphService
import com.orderagentservice.order.service.graph.screen.ScreenGraphService
import com.orderagentservice.order.service.graph.som.SomGraphService
import com.orderagentservice.order.service.graph.yolo.YoloGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate


@Component
class UiDetectorManager @Autowired constructor(
    private val env: Environment,
    private val notificationService: NotificationService,
    private val screenGraphService: ScreenGraphService,
    private val yoloGraphService: YoloGraphService,
    private val ocrGraphService: OcrGraphService,
    private val somGraphService: SomGraphService
) {
    private val log = logger()

    private val UI_EXCTRACTOR_HOST = env.getProperty("ui-extractor.host")
    private val UI_EXTRACTOR_API_KEY = env.getProperty("ui-extractor.api-key")!!

    fun getUiComponents(context: GraphContext, extractType: ExtractType): MutableList<UiComponentDto> {
        //ui extractor에게 이미지 파싱 요청
        val captureDto = notificationService.sendCaptureCommand(context.kioskId)
        val imageBytes = captureDto.content
        val imageType = captureDto.type
        val uiResponse = queryUiExtractor(imageBytes, imageType, extractType.title)

        val uiElements = uiResponse.uiComponents
        val ocrElements = uiResponse.ocrComponents
        val yoloElements = uiResponse.yoloComponents

        //이미지 context에 매핑
        context.imageName = captureDto.url

        //노드 생성
        generateNode(
            context = context, captureDto = captureDto,
            uiComponents = uiElements, ocrComponents = ocrElements, yoloComponents = yoloElements,
        )

        //옴니파서에게 받은 이미지 적절히 변환
        val llmUiList = mutableListOf<UiComponentDto>()
        for (ele in uiElements) {
            var title = ""
            for (str in ele.contents) {
                title += str
            }

            val pixelCoordinate = ele.bbox.coordinate

            //TODO(여기서 중앙좌표가 아닌 바운딩박스 좌표로 반환)
            val cord = pixelCoordinate.getCenter()
            llmUiList.add(
                UiComponentDto(
                    x = cord.first, y = cord.second,
                    title = title,
                    minX = ele.bbox.coordinate.minX, minY = ele.bbox.coordinate.minY,
                    maxX = ele.bbox.coordinate.maxX, maxY = ele.bbox.coordinate.maxY,
                )
            )
        }
        return llmUiList
    }

    fun queryUiExtractor(imageByte: ByteArray, type: String, endpoint: String): DetectorResponse {
        val restTemplate = RestTemplate()
        val url = "$UI_EXCTRACTOR_HOST/v2/$endpoint"

        val fileContent = object : ByteArrayResource(imageByte) {
            override fun getFilename(): String {
                return "capture.${ImageUtils.getExtension(type)}"
            }
        }

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
                val uiComponents = response.data!!
                return uiComponents
            } catch (e: RuntimeException) {
                log.error(e.message)
                log.info("UI extractor service오류로 인해 재시도합니다. 현재횟수: $requestCount")
            }
            requestCount++
        }
        throw UiExtractException()
    }

    private fun generateNode(
        context: GraphContext,
        captureDto: KioskCaptureDto,
        uiComponents: List<DetectorUiComponentDto>,
        ocrComponents: List<DetectorUiComponentDto>,
        yoloComponents: List<DetectorUiComponentDto>,
    ) {
        val screenNodeId = screenGraphService.saveNode(
            ScreenDto(
                imageUrl = captureDto.url
            )
        ).id

        context.screenNodeId = screenNodeId

        //screen 노드에 박스 연결
        for (uiComponent in uiComponents) {
            val somNodeId = somGraphService.saveNode(
                SomDto(
                    minX = uiComponent.bbox.coordinate.minX, minY = uiComponent.bbox.coordinate.minY,
                    maxX = uiComponent.bbox.coordinate.maxX, maxY = uiComponent.bbox.coordinate.maxY,
                    content = uiComponent.contents
                )
            ).id
            screenGraphService.saveRel(screenNodeId, somNodeId)
        }
        for (ocrComponent in ocrComponents) {
            val ocrNodeId = ocrGraphService.saveNode(
                OcrDto(
                    minX = ocrComponent.bbox.coordinate.minX, minY = ocrComponent.bbox.coordinate.minY,
                    maxX = ocrComponent.bbox.coordinate.maxX, maxY = ocrComponent.bbox.coordinate.maxY,
                    content = ocrComponent.contents
                )
            ).id
            screenGraphService.saveRel(screenNodeId, ocrNodeId)
        }
        for (yoloComponent in yoloComponents) {
            val yoloNode = yoloGraphService.saveNode(
                YoloDto(
                    minX = yoloComponent.bbox.coordinate.minX, minY = yoloComponent.bbox.coordinate.minY,
                    maxX = yoloComponent.bbox.coordinate.maxX, maxY = yoloComponent.bbox.coordinate.maxY,
                )
            ).id
            screenGraphService.saveRel(screenNodeId, yoloNode)
        }
    }
}