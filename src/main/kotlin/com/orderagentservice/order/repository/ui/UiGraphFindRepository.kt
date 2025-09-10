package com.orderagentservice.order.repository.ui

import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.model.type.SpecialNodeType
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param

interface UiGraphFindRepository {
    @Query(
        "MATCH (start:UI {kioskId:\$kioskId, id:\$sourceId})\n" +
                "MATCH (t:UI {kioskId:\$kioskId, title:\$targetTitle})\n" +
                "WITH start, t\n" +
                "MATCH p1 = shortestPath( (start)-[*..6]->(t) )\n" +
                "WITH start, t, length(p1) AS dist\n" +
                "ORDER BY dist ASC\n" +
                "LIMIT 1\n" +
                "MATCH p = shortestPath( (start)-[*..6]->(t) )\n" +
                "RETURN nodes(p) AS nodes"
    )
    fun findPathByTitle(
        @Param("kioskId") kioskId: String,
        @Param("sourceId") sourceId: String,
        @Param("targetTitle") targetTitle: String = SpecialNodeType.ROOT.title
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
        @Param("title") title: String = SpecialNodeType.ROOT.title,
    ): UiEntity?

    @Query(
        "MATCH (n:UI {kioskId: \$kioskId, title: \$title})\n" +
                "RETURN n\n" +
                "LIMIT 1"
    )
    fun findStationNode(
        @Param("kioskId") kioskId: String,
        @Param("title") title: String = SpecialNodeType.STATION.title,
    ): UiEntity?

    @Query(
        "MATCH (n:UI {kioskId: \$kioskId, title: \$title})\n" +
                "RETURN n\n" +
                "LIMIT 1"
    )
    fun findNodeByTitle(
        @Param("kioskId") kioskId: String,
        @Param("title") title: String
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
        "MATCH(n: UI{kioskId: \$kioskId})\n" +
        "WHERE n.modified = true\n" +
        "RETURN n"
    )
    fun findModifiedNode(
        @Param("kioskId") kioskId: String,
    ): List<UiEntity>

    @Query(
        "MATCH(n: UI{kioskId: \$kioskId})\n" +
        "RETURN n"
    )
    fun findAllNode(
        @Param("kioskId") kioskId: String,
    ): List<UiEntity>

    @Query(
        "MATCH (n {kioskId: \$kioskId, id: \$sourceId})-[:BACK_TO]->()\n" +
                "RETURN COUNT(*) > 0 AS hasBackTo"
    )
    fun isBackRel(
        @Param("kioskId") kioskId: String,
        @Param("sourceId") sourceId: String
    ): Boolean?

    @Query("MATCH(n: UI{kioskId: \$kioskId})-[r]->(m) RETURN n,r,m")
    fun findAllByKioskId(kioskId: String): List<Map<String, Any>>
}