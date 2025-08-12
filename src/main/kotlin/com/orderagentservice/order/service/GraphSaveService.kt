package com.orderagentservice.order.service

import com.orderagentservice.order.utg.NodeRelation
import com.orderagentservice.order.utg.model.dto.UiDto
import com.orderagentservice.order.utg.model.entity.UiEntity

interface GraphSaveService {
    fun saveNode(uiDto: UiDto): UiEntity

    fun saveRel(sourceId: String, targetId: String, type: NodeRelation)
}