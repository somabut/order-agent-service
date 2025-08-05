package com.orderagentservice.order.model.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.*
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

@Node("UI")
class UiEntity(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Version
    val version: Long? = null,

    @Property("is_next")
    val isNext: Boolean,

    @Property("x")
    val x: Int,

    @Property("y")
    val y: Int,

    @Property("title")
    val title: String,

    @Property("kioskId")
    val kioskId: String,

    @CreatedDate
    @Property("created_at")
    val createdAt: OffsetDateTime? = null,

    @Relationship(type = "PATH_TO", direction = Relationship.Direction.OUTGOING)
    val connectedTo: Set<UiEntity>? = null,

    @Relationship(type = "HAS_TO", direction = Relationship.Direction.OUTGOING)
    val hasTo: Set<UiEntity>? = null,

    @Relationship(type = "BACK_TO", direction = Relationship.Direction.OUTGOING)
    val backTo: Set<UiEntity>? = null,
)