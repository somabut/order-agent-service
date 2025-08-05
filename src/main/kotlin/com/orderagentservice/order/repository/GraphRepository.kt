package com.orderagentservice.order.repository

import com.orderagentservice.order.model.entity.UiEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param

interface GraphRepository : Neo4jRepository<UiEntity, String> {
    @Query(
        "MATCH path = (start:UI {kioskId: \$kioskId, id: \$sourceId})-[*]->(target:UI {kioskId: \$kioskId, title: \$targetTitle})\n " +
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

    @Query(
        "MATCH (currentNode {kioskId: \$kioskId, id: \$sourceId})-[:HAS_TO]->(targetNode {kioskId: \$kioskId, title: \$place})\n" +
        "RETURN targetNode"
    )
    fun findPlaceByTitle(
        @Param("kioskId") kioskId: String,
        @Param("sourceId") sourceId: String,
        @Param("place") place: String
    ): UiEntity?

    @Query(
        "MATCH (n:UI {kioskId: \$kioskId, title: \$title})\n" +
        "RETURN n\n" +
        "LIMIT 1"
    )
    fun findRootNode(
        @Param("kioskId") kioskId: String,
        @Param("title") title: String = "root",
    ): UiEntity?

    @Query(
        "MATCH (n:UI {kioskId: \$kioskId, title: \$title})\n" +
        "RETURN n\n" +
        "LIMIT 1"
    )
    fun findStationNode(
        @Param("kioskId") kioskId: String,
        @Param("title") title: String = "station",
    ): UiEntity?

    @Query(
        "MATCH (prev:UI)-[:BACK_TO]->(current:UI {kioskId: \$kioskId, id: \$id})\n" +
        "RETURN prev\n" +
        "LIMIT 1"
    )
    fun findIncomingBackTo(
        @Param("kioskId") kioskId: String,
        @Param("id") id: String
    ): UiEntity?

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

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:PATH_TO]->(b)")
    fun savePathRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:HAS_TO]->(b)")
    fun saveHasRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:OPT_TO]->(b)")
    fun saveOptRelation(sourceId: String, targetId: String)

    @Query("MATCH (a:UI {id: \$sourceId}), (b:UI {id: \$targetId}) MERGE (a)-[:BACK_TO]->(b)")
    fun saveBackRelation(sourceId: String, targetId: String)

    @Query("MATCH(n: UI{kioskId: \$kioskId})-[r]->(m) RETURN n,r,m")
    fun findAllByKioskId(kioskId: String): List<Map<String, Any>>
}