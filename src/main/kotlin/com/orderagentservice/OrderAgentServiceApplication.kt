package com.orderagentservice

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.neo4j.config.EnableNeo4jAuditing

@EnableNeo4jAuditing
@SpringBootApplication
class OrderAgentServiceApplication

inline fun <reified T> T.logger() = LoggerFactory.getLogger(T::class.java)!!
val jsonMapper = jacksonObjectMapper().apply {
    configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false)
}

fun main(args: Array<String>) {
    runApplication<OrderAgentServiceApplication>(*args)
}
