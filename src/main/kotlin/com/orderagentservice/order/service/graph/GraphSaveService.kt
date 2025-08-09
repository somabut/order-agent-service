package com.orderagentservice.order.service.graph

import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity

interface GraphSaveService {
    fun saveNode(uiDto: UiDto): UiEntity

    fun saveRel(sourceId: String, targetId: String, type: NodeRelation)
}