package com.orderagentservice.order.service.graph.som

import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.model.entity.SomEntity
import com.orderagentservice.order.repository.som.SomGraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SomGraphServiceImpl @Autowired constructor(
    private val somGraphRepository: SomGraphRepository
) : SomGraphService {
    override fun saveNode(somDto: SomDto): SomEntity {
        val somEntity = somGraphRepository.save(somDto.toEntity())
        return somEntity
    }
}