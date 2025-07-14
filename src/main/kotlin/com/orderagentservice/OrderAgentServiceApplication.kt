package com.orderagentservice

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import com.fasterxml.jackson.module.kotlin.readValue

@SpringBootApplication
class OrderAgentServiceApplication

inline fun <reified T> T.logger() = LoggerFactory.getLogger(T::class.java)!!
val jsonMapper = jacksonObjectMapper()

fun main(args: Array<String>) {
    runApplication<OrderAgentServiceApplication>(*args)
}
