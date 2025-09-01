package com.orderagentservice.order.repository.yolo

import com.orderagentservice.order.model.entity.YoloEntity
import org.springframework.data.neo4j.repository.Neo4jRepository

interface YoloGraphRepository : Neo4jRepository<YoloEntity, String> {
}