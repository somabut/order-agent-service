package com.orderagentservice.order.repository.screen

import com.orderagentservice.order.model.entity.ScreenEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param

interface ScreenGraphRepository : Neo4jRepository<ScreenEntity, String> {

    @Query(
        "MATCH(n: UI{kioskId: \$kioskId, id: \$sourceId})-[r:IMAGE_TO]->(m:Screen)\n" +
                "RETURN m\n" +
                "LIMIT 1"
    )
    fun findLinkedScreenNode(
        @Param("kioskId") kioskId: String,
        @Param("sourceId") sourceId: String
    ): ScreenEntity?

    @Query("MATCH (a:Screen {id: \$sourceId}), (b:SOM {id: \$targetId}) MERGE (a)-[:BOX_TO]->(b)")
    fun saveBoxSomRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:Screen {id: \$sourceId}), (b:OCR {id: \$targetId}) MERGE (a)-[:BOX_TO]->(b)")
    fun saveBoxOcrRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:Screen {id: \$sourceId}), (b:YOLO {id: \$targetId}) MERGE (a)-[:BOX_TO]->(b)")
    fun saveBoxYoloRelation(sourceId: String, targetId: String)
}