package com.orderagentservice.order.service.graph.info

import com.orderagentservice.order.exception.NodeNotFoundException
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.dto.InfoDto
import com.orderagentservice.order.model.entity.InfoEntity
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.repository.info.InfoGraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class InfoGraphServiceImpl @Autowired constructor(
    private val infoGraphRepository: InfoGraphRepository
) : InfoGraphService {
    override fun saveNode(infoDto: InfoDto): InfoEntity {
        val entity = infoGraphRepository.save(infoDto.toEntity())
        return entity
    }

    override fun saveRel(kioskId: String, infoId: String) {
        infoGraphRepository.saveInfoRelation(kioskId, infoId)
    }

    override fun findLinkedInfo(kioskId: String): UtgStrategyRequest {
        val entity = infoGraphRepository.findLinkedInfoNode(kioskId) ?: throw NodeNotFoundException()
        return UtgStrategyRequest(
            startStrategy = entity.startStrategy,
            optionStrategy = entity.optionStrategy,
            backStrategy = entity.backStrategy,
            paymentStrategy = entity.paymentStrategy,
        )
    }
}