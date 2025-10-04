package com.orderagentservice.order.repository.info

import com.orderagentservice.order.model.entity.InfoEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param

interface InfoGraphRepository : Neo4jRepository<InfoEntity, String> {
    @Query(
        "MATCH (a:UI {kioskId: \$kioskId, title: 'root'})\n" +
        "MATCH (b:INFO {id: \$infoId})\n" +
        "MERGE (a)-[:INFO_TO]->(b)"
    )
    fun saveInfoRelation(
        @Param("kioskId") kioskId: String,
        @Param("infoId") infoId: String
    )
}