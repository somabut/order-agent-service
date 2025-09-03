package com.orderagentservice.order.service.graph.ocr

import com.orderagentservice.order.model.dto.OcrDto
import com.orderagentservice.order.model.entity.OcrEntity

interface OcrGraphService {
    fun saveNode(ocrDto: OcrDto): OcrEntity
    fun findNode(kioskId: String, minX: Int, minY: Int, maxX: Int, maxY: Int, title: String): String
}