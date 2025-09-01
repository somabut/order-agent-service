package com.orderagentservice.order.service.graph.screen

import com.orderagentservice.order.model.dto.ScreenDto
import com.orderagentservice.order.model.entity.ScreenEntity

interface ScreenGraphService {
    fun saveNode(screenDto: ScreenDto): ScreenEntity
}