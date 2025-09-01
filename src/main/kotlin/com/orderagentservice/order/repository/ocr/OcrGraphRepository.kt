package com.orderagentservice.order.repository.ocr

import com.orderagentservice.order.model.entity.OcrEntity
import org.springframework.data.neo4j.repository.Neo4jRepository

interface OcrGraphRepository : Neo4jRepository<OcrEntity, String> {
}