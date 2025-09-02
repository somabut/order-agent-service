package com.orderagentservice.order.service.graph.ocr

import com.orderagentservice.order.model.dto.OcrDto
import com.orderagentservice.order.model.entity.OcrEntity

interface OcrGraphService {
    fun saveNode(ocrDto: OcrDto): OcrEntity
}