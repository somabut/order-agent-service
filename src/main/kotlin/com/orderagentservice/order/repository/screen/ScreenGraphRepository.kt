package com.orderagentservice.order.repository.screen

import com.orderagentservice.order.model.entity.ScreenEntity
import org.springframework.data.neo4j.repository.Neo4jRepository

interface ScreenGraphRepository : Neo4jRepository<ScreenEntity, String> {
}