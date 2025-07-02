package com.orderagentservice.order.model.entity

import org.springframework.data.neo4j.core.schema.*
import java.util.*

@Node("UI")
class UiEntity (
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Property("is_next")
    val isNext: Boolean,

    @Property("x")
    val x: Int,

    @Property("y")
    val y: Int,

    @Property("title")
    val title: String,

    @Property("url")
    val url: String,

    @Relationship(type = "PATH_TO", direction = Relationship.Direction.OUTGOING)
    val connectedTo: Set<UiEntity>? = null,

    @Relationship(type = "HAS_TO", direction = Relationship.Direction.OUTGOING)
    val hasTo: Set<UiEntity>? = null
)