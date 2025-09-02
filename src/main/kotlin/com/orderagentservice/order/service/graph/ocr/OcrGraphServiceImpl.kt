package com.orderagentservice.order.service.graph.ocr

import com.orderagentservice.order.model.dto.OcrDto
import com.orderagentservice.order.model.entity.OcrEntity
import com.orderagentservice.order.repository.ocr.OcrGraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OcrGraphServiceImpl @Autowired constructor (
    private val ocrGraphRepository: OcrGraphRepository
) : OcrGraphService {
    override fun saveNode(ocrDto: OcrDto): OcrEntity {
        val ocrEntity = ocrGraphRepository.save(ocrDto.toEntity())
        return ocrEntity
    }
}