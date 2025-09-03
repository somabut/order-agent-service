package com.orderagentservice.order.repository.ocr

import com.orderagentservice.order.model.entity.OcrEntity
import com.orderagentservice.order.model.entity.SomEntity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

interface OcrGraphRepository : Neo4jRepository<OcrEntity, String> {
    @Query(
        "MATCH(n: BOX:OCR{kioskId: \$kioskId, min_x: \$minX, min_y: \$minY, max_x: \$maxX, max_y: \$maxY, content: \$title})\n" +
        "RETURN n\n" +
        "LIMIT 1"
    )
    fun findByBboxAndTitle(
        kioskId: String,
        minX: Int, minY: Int,
        maxX: Int, maxY: Int,
        title: String
    ): OcrEntity?
}