package com.orderagentservice.order.repository.screen

import com.orderagentservice.order.model.entity.ScreenEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

interface ScreenGraphRepository : Neo4jRepository<ScreenEntity, String> {

    @Query("MATCH (a:Screen {id: \$sourceId}), (b:Screen {id: \$targetId}) MERGE (a)-[:BOX_TO]->(b)")
    fun saveBoxRelation(sourceId: String, targetId: String)
}