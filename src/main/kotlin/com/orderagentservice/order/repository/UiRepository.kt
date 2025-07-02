package com.orderagentservice.order.repository

import com.orderagentservice.order.model.entity.UiEntity
import org.neo4j.driver.types.Path
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param

interface UiRepository : Neo4jRepository<UiEntity, String> {
    @Query(
        "MATCH path = (start:UI {title: \$rootTitle})-[*..5]->(target:UI) " +
        "WHERE target.title CONTAINS \$target AND target.url = \$url " +
        "RETURN path"
    )
    fun findPathByTitle(
        @Param("url") url: String,
        @Param("target") target: String,
        @Param("rootTitle") rootTitle: String = "root"
    ): List<Path>

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:PATH_TO]->(b)")
    fun savePathRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:HAS_TO]->(b)")
    fun saveHasRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:OPT_TO]->(b)")
    fun saveOptRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:BACK_TO]->(b)")
    fun saveBackRelation(sourceId: String, targetId: String)
}