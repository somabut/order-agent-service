package com.orderagentservice.order.repository.som

import com.orderagentservice.order.model.entity.SomEntity
import org.springframework.data.neo4j.repository.Neo4jRepository

interface SomGraphRepository : Neo4jRepository<SomEntity, String> {
}