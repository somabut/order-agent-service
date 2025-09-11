package com.orderagentservice.order.service.graph.screen

import com.orderagentservice.order.model.dto.ScreenDto
import com.orderagentservice.order.model.entity.ScreenEntity
import com.orderagentservice.order.model.type.NodeType

interface ScreenGraphService {
    fun saveNode(screenDto: ScreenDto): ScreenEntity
    fun saveRel(sourceId: String, targetId: String, nodeType: NodeType)
    fun findLinkedScreen(kioskId: String, sourceId: String): String
}