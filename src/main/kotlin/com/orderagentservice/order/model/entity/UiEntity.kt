package com.orderagentservice.order.model.entity

import com.orderagentservice.agent.model.dto.AgentActionDto
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.*
import java.time.*
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

    @Property("type")
    val type: String,

    @Property("modified")
    val modified: Boolean,

    @Property("created_at")
    val createdAt: OffsetDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toOffsetDateTime(),

    @Property("updated_at")
    val updatedAt: OffsetDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toOffsetDateTime(),

    @Relationship(type = "PATH_TO", direction = Relationship.Direction.OUTGOING)
    val connectedTo: Set<UiEntity>? = null,

    @Relationship(type = "HAS_TO", direction = Relationship.Direction.OUTGOING)
    val hasTo: Set<UiEntity>? = null,

    @Relationship(type = "BACK_TO", direction = Relationship.Direction.OUTGOING)
    val backTo: Set<UiEntity>? = null,
) {
    fun toAgentActionDto(minX: Int, minY: Int, maxX: Int, maxY: Int) = AgentActionDto(
        goNext = isNext,
        score = 0.0F,
        coordinate = listOf(x, y),
        bbox = listOf(minX, minY, maxX, maxY),
        title = title
    )
}