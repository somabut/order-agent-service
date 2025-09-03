package com.orderagentservice.order.service.graph.ocr

import com.orderagentservice.order.exception.NodeNotFoundException
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

    override fun findNode(kioskId: String, minX: Int, minY: Int, maxX: Int, maxY: Int, title: String): String {
        val ocrEntity = ocrGraphRepository.findByBboxAndTitle(
            kioskId = kioskId,
            minX = minX, minY = minY,
            maxX = maxX, maxY = maxY,
            title = title
        ) ?: throw NodeNotFoundException()

        return ocrEntity.id
    }
}