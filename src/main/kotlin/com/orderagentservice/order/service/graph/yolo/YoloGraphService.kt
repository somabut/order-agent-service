package com.orderagentservice.order.service.graph.yolo

import com.orderagentservice.order.model.dto.YoloDto
import com.orderagentservice.order.model.entity.YoloEntity

interface YoloGraphService {
    fun saveNode(yoloDto: YoloDto): YoloEntity
}