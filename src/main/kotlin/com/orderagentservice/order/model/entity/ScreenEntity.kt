package com.orderagentservice.order.model.entity

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@Node("Screen")
class ScreenEntity (
    @Id
    val id: String = UUID.randomUUID().toString(),

    val kioskId: String,

    @Version
    val version: Long? = null,

    @Property("image_url")
    val imageUrl: String,

    @Property("created_at")
    val createdAt: OffsetDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toOffsetDateTime(),

    @Property("updated_at")
    val updatedAt: OffsetDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toOffsetDateTime(),
)