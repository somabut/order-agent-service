package com.orderagentservice.order.service.graph.screen

import com.orderagentservice.order.model.dto.ScreenDto
import com.orderagentservice.order.model.entity.ScreenEntity
import com.orderagentservice.order.repository.screen.ScreenGraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ScreenGraphServiceImpl @Autowired constructor(
    private val screenGraphRepository: ScreenGraphRepository
) : ScreenGraphService {
    override fun saveNode(screenDto: ScreenDto): ScreenEntity {
        val screenEntity = screenGraphRepository.save(screenDto.toEntity())
        return screenEntity
    }
}