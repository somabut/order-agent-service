package com.orderagentservice.order.repository.som

import com.orderagentservice.order.model.entity.SomEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

interface SomGraphRepository : Neo4jRepository<SomEntity, String> {

    @Query(
        "MATCH (s:Screen {id: \$sourceId})-[:BOX_TO]->(n:BOX:SOM)\n" +
        "WHERE n.kioskId = \$kioskId\n" +
        "  AND n.min_x = \$minX\n" +
        "  AND n.min_y = \$minY\n" +
        "  AND n.max_x = \$maxX\n" +
        "  AND n.max_y = \$maxY\n" +
        "  AND n.content = \$title\n" +
        "RETURN n\n" +
        "LIMIT 1"
    )
    fun findByBboxAndTitle(
        sourceId: String,
        kioskId: String,
        minX: Int, minY: Int,
        maxX: Int, maxY: Int,
        title: String
    ): SomEntity?
}