package com.orderagentservice.order.model.entity

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@Node("YOLO", "BOX")
class YoloEntity (
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Version
    val version: Long? = null,

    @Property("min_x")
    val minX: Int,

    @Property("min_y")
    val minY: Int,

    @Property("max_x")
    val maxX: Int,

    @Property("max_y")
    val maxY: Int,

    @Property("created_at")
    val createdAt: OffsetDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toOffsetDateTime(),

    @Property("updated_at")
    val updatedAt: OffsetDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toOffsetDateTime(),
)