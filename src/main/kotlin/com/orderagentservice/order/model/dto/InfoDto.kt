package com.orderagentservice.order.model.dto

import com.orderagentservice.order.model.entity.InfoEntity
import com.orderagentservice.order.model.entity.OcrEntity
import org.springframework.data.neo4j.core.schema.Property

data class InfoDto (
    val startStrategy: String,
    val optionStrategy: String,
    val backStrategy: String,
    val paymentStrategy: String,
) {
    fun toEntity() = InfoEntity(
        startStrategy = startStrategy,
        optionStrategy = optionStrategy,
        backStrategy = backStrategy,
        paymentStrategy = paymentStrategy,
    )
}