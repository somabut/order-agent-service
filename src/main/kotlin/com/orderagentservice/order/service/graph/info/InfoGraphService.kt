package com.orderagentservice.order.service.graph.info

import com.orderagentservice.order.model.dto.InfoDto
import com.orderagentservice.order.model.entity.InfoEntity

interface InfoGraphService {
    fun saveNode(infoDto: InfoDto): InfoEntity
    fun saveRel(kioskId: String, infoId: String)
}