package com.orderagentservice.order.service.utg

import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.service.graph.ocr.OcrGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OcrNodeGenerator @Autowired constructor(
    private val uiGraphService: UiGraphService,
    private val ocrGraphService: OcrGraphService
) {
    fun linkNode(kioskId: String, nodeId: String, screenNodeId: String, uiComponentParams: UiComponentParams) {
        val ocrNodeId = ocrGraphService.findNode(
            kioskId = kioskId,
            minX = uiComponentParams.minX, minY = uiComponentParams.minY,
            maxX = uiComponentParams.maxX, maxY = uiComponentParams.maxY,
            title = uiComponentParams.title
        )
        uiGraphService.saveRel(nodeId, ocrNodeId, NodeRelationType.MATCH_TO)
        uiGraphService.saveRel(nodeId, screenNodeId, NodeRelationType.IMAGE_TO)
    }
}