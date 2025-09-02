package com.orderagentservice.order.service.graph.yolo

import com.orderagentservice.order.model.dto.YoloDto
import com.orderagentservice.order.model.entity.YoloEntity
import com.orderagentservice.order.repository.yolo.YoloGraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class YoloGraphServiceImpl @Autowired constructor(
    private val yoloGraphRepository: YoloGraphRepository
) : YoloGraphService {
    override fun saveNode(yoloDto: YoloDto): YoloEntity {
        val yoloEntity = yoloGraphRepository.save(yoloDto.toEntity())
        return yoloEntity
    }
}