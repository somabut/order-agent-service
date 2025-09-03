package com.orderagentservice.order.repository.screen

import com.orderagentservice.order.model.entity.ScreenEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

interface ScreenGraphRepository : Neo4jRepository<ScreenEntity, String> {

    @Query("MATCH (a:Screen {id: \$sourceId}), (b:SOM {id: \$targetId}) MERGE (a)-[:BOX_TO]->(b)")
    fun saveBoxSomRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:Screen {id: \$sourceId}), (b:OCR {id: \$targetId}) MERGE (a)-[:BOX_TO]->(b)")
    fun saveBoxOcrRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:Screen {id: \$sourceId}), (b:YOLO {id: \$targetId}) MERGE (a)-[:BOX_TO]->(b)")
    fun saveBoxYoloRelation(sourceId: String, targetId: String)
}