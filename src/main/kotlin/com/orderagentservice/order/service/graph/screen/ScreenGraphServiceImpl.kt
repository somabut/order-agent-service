package com.orderagentservice.order.service.graph.screen

import com.orderagentservice.logger
import com.orderagentservice.order.exception.NodeNotFoundException
import com.orderagentservice.order.model.dto.ScreenDto
import com.orderagentservice.order.model.entity.ScreenEntity
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.repository.screen.ScreenGraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ScreenGraphServiceImpl @Autowired constructor(
    private val screenGraphRepository: ScreenGraphRepository
) : ScreenGraphService {
    private val log = logger()

    override fun saveNode(screenDto: ScreenDto): ScreenEntity {
        val screenEntity = screenGraphRepository.save(screenDto.toEntity())
        return screenEntity
    }

    override fun saveRel(
        sourceId: String,
        targetId: String,
        nodeType: NodeType
    ) {
        log.info("관계 설정. ${sourceId} [${NodeRelationType.BOX_TO}]-> ${targetId}")
        when(nodeType) {
            NodeType.SOM -> screenGraphRepository.saveBoxSomRelation(sourceId, targetId)
            NodeType.OCR -> screenGraphRepository.saveBoxOcrRelation(sourceId, targetId)
            NodeType.YOLO -> screenGraphRepository.saveBoxYoloRelation(sourceId, targetId)
            else -> throw NodeNotFoundException()
        }
    }
}