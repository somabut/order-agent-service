package com.orderagentservice.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.core.convert.Neo4jConversions
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class Neo4jConfig {
    fun customConversions(): Neo4jConversions {
        return Neo4jConversions(listOf(StringToBooleanConverter()))
    }

    @Bean
    fun neo4jTemplate(
        neo4jClient: Neo4jClient,
        mappingContext: Neo4jMappingContext,
        transactionManager: PlatformTransactionManager
    ): Neo4jTemplate {
        return Neo4jTemplate(neo4jClient, mappingContext, transactionManager)
    }
}