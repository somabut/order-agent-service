package com.orderagentservice.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.convert.Neo4jConversions

@Configuration
class Neo4jConfig {
    fun customConversions(): Neo4jConversions {
        return Neo4jConversions(listOf(StringToBooleanConverter()))
    }
}