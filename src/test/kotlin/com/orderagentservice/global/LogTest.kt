package com.orderagentservice.global

import com.orderagentservice.global.util.GlobalLogger
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LogTest @Autowired constructor(
    private val globalLogger: GlobalLogger
) {
    @Test
    fun `asdfas`() {
        globalLogger.loggingOrderStart("kiosk123", "task123")
    }
}