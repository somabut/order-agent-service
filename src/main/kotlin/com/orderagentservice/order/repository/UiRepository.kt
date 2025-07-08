package com.orderagentservice.order.repository

import com.orderagentservice.order.model.entity.UiEntity
import org.neo4j.driver.types.Path
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param

interface UiRepository : Neo4jRepository<UiEntity, String> {
    @Query(
        "MATCH path = (start:UI {kioskId: \$kioskId, title: \$sourceId})-[*..5]->(target:UI {kioskId: \$kioskId})\n " +
        "WHERE target.title CONTAINS \$targetTitle\n" +
        "RETURN nodes(path) AS nodes\n" +
        "ORDER BY length(path) ASC\n" +
        "LIMIT 1"
    )
    fun findPathByTitle(
        @Param("kioskId") kioskId: String,
        @Param("sourceId") sourceId: String,
        @Param("targetTitle") targetTitle: String = "root"
    ): List<Map<String, Any>>

    @Query(
        "MATCH (menu:UI {kioskId: \$kioskId, id: \$menuId})-[:OPT_TO]->(opt:UI {kioskId: \$kioskId})\n" +
        "WHERE opt.title CONTAINS \$optionKeyword\n" +
        "RETURN opt\n" +
        "LIMIT 1"
    )
    fun findOptionByTitle(
        @Param("kioskId") kioskId: String,
        @Param("menuId") menuId: String,
        @Param("optionKeyword") optionKeyword: String,
    ): UiEntity?

    @Query(
        "MATCH path = (start:UI {kioskId: \$kioskId, id: \$sourceId})-[:BACK_TO*2]->(target:UI {kioskId: \$kioskId})\n" +
        "RETURN nodes(path) AS nodes\n" +
        "LIMIT 1"
    )
    fun findBackPathByTitle(
        @Param("kioskId") kioskId: String,
        @Param("sourceId") sourceId: String
    ): List<Map<String, Any>>

    @Query(
        "MATCH (prev:UI)-[:HAS_TO]->(current:UI {kioskId: \$kioskId, id: \$id})\n" +
        "RETURN prev\n" +
        "LIMIT 1"
    )
    fun findIncomingHasTo(
        @Param("kioskId") kioskId: String,
        @Param("id") id: String
    ): UiEntity?

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:PATH_TO]->(b)")
    fun savePathRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:HAS_TO]->(b)")
    fun saveHasRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:OPT_TO]->(b)")
    fun saveOptRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:BACK_TO]->(b)")
    fun saveBackRelation(sourceId: String, targetId: String)
}