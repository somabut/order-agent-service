package com.orderagentservice.order.repository.som

import com.orderagentservice.order.model.entity.SomEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

interface SomGraphRepository : Neo4jRepository<SomEntity, String> {

    @Query(
        "MATCH(n: SOM{min_x: \$minX, min_y: \$minY, max_x: \$maxX, max_y: \$maxY, title: \$title})\n" +
        "RETURN n\n" +
        "LIMIT 1"
    )
    fun findByBboxAndTitle(
        minX: Int, minY: Int,
        maxX: Int, maxY: Int,
        title: String
    ): SomEntity?
}