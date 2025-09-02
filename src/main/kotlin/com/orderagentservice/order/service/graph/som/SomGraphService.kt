package com.orderagentservice.order.service.graph.som

import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.model.entity.SomEntity
import com.orderagentservice.order.model.type.NodeRelationType

interface SomGraphService {
    fun saveNode(somDto: SomDto): String

    fun findNode(minX: Int, minY: Int, maxX: Int, maxY: Int, title: String): String
}