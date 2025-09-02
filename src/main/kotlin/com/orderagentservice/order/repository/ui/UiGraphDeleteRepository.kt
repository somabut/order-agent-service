package com.orderagentservice.order.repository.ui

import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.query.Param

@NoRepositoryBean
interface UiGraphDeleteRepository {
    @Query(
        "MATCH (n {id: \$sourceId, kioskId: \$kioskId})-[r:HAS_TO]->(m)\n" +
                "OPTIONAL MATCH (m)-[:opt_TO]->(x)\n" +
                "OPTIONAL MATCH (n)<-[:BACK_TO]-(y)\n" +
                "DETACH DELETE m, x, y"
    )
    fun deleteMenuNode(
        @Param("sourceId") sourceId: String,
        @Param("kioskId") kioskId: String,
    )
}