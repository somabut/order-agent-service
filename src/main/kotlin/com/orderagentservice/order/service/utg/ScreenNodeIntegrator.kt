package com.orderagentservice.order.service.utg

import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.DetectorUiComponentDto
import com.orderagentservice.order.model.dto.KioskCaptureDto
import com.orderagentservice.order.model.dto.OcrDto
import com.orderagentservice.order.model.dto.ScreenDto
import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.dto.YoloDto
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.graph.ocr.OcrGraphService
import com.orderagentservice.order.service.graph.screen.ScreenGraphService
import com.orderagentservice.order.service.graph.som.SomGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.graph.yolo.YoloGraphService
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ScreenNodeIntegrator @Autowired constructor(
    private val screenGraphService: ScreenGraphService,
    private val somGraphService: SomGraphService,
    private val ocrGraphService: OcrGraphService,
    private val yoloGraphService: YoloGraphService,
    private val uiGraphService: UiGraphService
) {
    private val log = logger()

    @Transactional
    fun integrateScreenNode(
        context: GraphContext,
        captureDto: KioskCaptureDto,
        uiComponents: List<DetectorUiComponentDto>,
        ocrComponents: List<DetectorUiComponentDto>,
        yoloComponents: List<DetectorUiComponentDto>,
    ) {
        val screenNodeId = screenGraphService.saveNode(
            ScreenDto(
                kioskId = context.kioskId,
                imageUrl = captureDto.url
            )
        ).id

        context.screenNodeId = screenNodeId

        //screen 노드에 박스 연결
        connectSom(context, uiComponents)
        connectOcr(context, ocrComponents)
        connectYolo(context, yoloComponents)
    }

    @Transactional
    fun linkNode(kioskId: String, nodeId: String, screenNodeId: String, uiComponentParams: UiComponentParams) {
        //match 노드와 관계, screen 노드와 관계 연결
        val minX = uiComponentParams.minX
        val minY = uiComponentParams.minY
        val maxX = uiComponentParams.maxX
        val maxY = uiComponentParams.maxY
        log.info("${uiComponentParams.title} 의 연결을 조회합니다. bbox: [${minX}, ${minY}, ${maxX}, ${maxY}]")
        val somNodeId = somGraphService.findNode(
            sourceId = screenNodeId,
            kioskId = kioskId,
            minX = minX, minY = minY,
            maxX = maxX, maxY = maxY,
            title = uiComponentParams.title
        )
        uiGraphService.saveRel(nodeId, somNodeId, NodeRelationType.MATCH_TO)
        uiGraphService.saveRel(nodeId, screenNodeId, NodeRelationType.IMAGE_TO)
    }

    private fun connectSom(context: GraphContext, uiComponents: List<DetectorUiComponentDto>) {
        log.info("SOM을 Screen에 연결. screen: ${context.screenNodeId}")
        for (uiComponent in uiComponents) {
            val somNodeId = somGraphService.saveNode(
                SomDto(
                    kioskId = context.kioskId,
                    minX = uiComponent.bbox.coordinate.minX, minY = uiComponent.bbox.coordinate.minY,
                    maxX = uiComponent.bbox.coordinate.maxX, maxY = uiComponent.bbox.coordinate.maxY,
                    content = uiComponent.contents
                )
            )
            screenGraphService.saveRel(context.screenNodeId, somNodeId, NodeType.SOM)
        }
    }

    private fun connectOcr(context: GraphContext, ocrComponents: List<DetectorUiComponentDto>) {
        log.info("OCR을 Screen에 연결. screen: ${context.screenNodeId}")
        for (ocrComponent in ocrComponents) {
            val ocrNodeId = ocrGraphService.saveNode(
                OcrDto(
                    minX = ocrComponent.bbox.coordinate.minX, minY = ocrComponent.bbox.coordinate.minY,
                    maxX = ocrComponent.bbox.coordinate.maxX, maxY = ocrComponent.bbox.coordinate.maxY,
                    content = ocrComponent.contents
                )
            ).id
            screenGraphService.saveRel(context.screenNodeId, ocrNodeId, NodeType.OCR)
        }
    }

    private fun connectYolo(context: GraphContext, yoloComponents: List<DetectorUiComponentDto>) {
        log.info("YOLO을 Screen에 연결. screen: ${context.screenNodeId}")
        for (yoloComponent in yoloComponents) {
            val yoloNode = yoloGraphService.saveNode(
                YoloDto(
                    kioskId = context.kioskId,
                    minX = yoloComponent.bbox.coordinate.minX, minY = yoloComponent.bbox.coordinate.minY,
                    maxX = yoloComponent.bbox.coordinate.maxX, maxY = yoloComponent.bbox.coordinate.maxY,
                )
            ).id
            screenGraphService.saveRel(context.screenNodeId, yoloNode, NodeType.YOLO)
        }
    }
}