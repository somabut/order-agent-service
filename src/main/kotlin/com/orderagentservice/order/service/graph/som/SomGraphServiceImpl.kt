package com.orderagentservice.order.service.graph.som

import com.orderagentservice.order.exception.NodeNotFoundException
import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.repository.som.SomGraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SomGraphServiceImpl @Autowired constructor(
    private val somGraphRepository: SomGraphRepository
) : SomGraphService {
    override fun saveNode(somDto: SomDto): String {
        val somEntity = somGraphRepository.save(somDto.toEntity())
        return somEntity.id
    }

    override fun findNode(
        sourceId: String,
        kioskId: String,
        minX: Int, minY: Int,
        maxX: Int, maxY: Int,
        title: String
    ): String {
        val somEntity = somGraphRepository.findByBboxAndTitle(
            sourceId = sourceId,
            kioskId = kioskId,
            minX = minX, minY = minY,
            maxX = maxX, maxY = maxY,
            title = title
        ) ?: throw NodeNotFoundException()

        return somEntity.id
    }
}