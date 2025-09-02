package com.orderagentservice.order.repository.ui

import org.springframework.data.neo4j.repository.query.Query

interface UiGraphSaveRepository {
    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:PATH_TO]->(b)")
    fun savePathRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:HAS_TO]->(b)")
    fun saveHasRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:OPT_TO]->(b)")
    fun saveOptRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:BACK_TO]->(b)")
    fun saveBackRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:MATCH_TO]->(b)")
    fun saveMathRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:IMAGE_TO]->(b)")
    fun saveImageRelation(sourceId: String, targetId: String)
}