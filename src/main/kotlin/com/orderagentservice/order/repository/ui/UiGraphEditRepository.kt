package com.orderagentservice.order.repository.ui

import com.orderagentservice.order.model.entity.UiEntity
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.query.Param

@NoRepositoryBean
interface UiGraphEditRepository {
    @Query(
        "MATCH (n:UI {id: \$nodeId, kioskId: \$kioskId})\n" +
                "SET n.title = \$title\n" +
                "RETURN n"
    )
    fun changeTitleById(
        @Param("nodeId") nodeId: String,
        @Param("kioskId") kioskId: String,
        @Param("title") title: String,
    ): UiEntity?

    @Query(
        "MATCH (n:UI {kioskId: \$kioskId, title: \$title})\n" +
        "SET n.modified = \$modified"
    )
    fun changeModifiedByTitle(
        @Param("kioskId") kioskId: String,
        @Param("title") title: String,
        @Param("modified") modified: Boolean,
    )
}