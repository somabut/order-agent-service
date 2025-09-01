package com.orderagentservice.order.service.graph.som

import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.model.entity.SomEntity

interface SomGraphService {
    fun saveNode(somDto: SomDto): SomEntity
}