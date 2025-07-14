package com.orderagentservice.global

import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.order.repository.MenuRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LogTest @Autowired constructor(
    private val globalLogger: GlobalLogger,
    private val menuRepository: MenuRepository
) {
    @Test
    fun `afasdfasdf`() {
        val kioskId = "kiosk-8f22eeb6-a920-44ed-af26-d800756fb283"
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl8wMDIiLCJpYXQiOjE3NTI0MjMzNDMsImV4cCI6MTc1MjUwOTc0M30.ga5z9uXFIBLmqVHSZr1kRFTXAxPAX4Y5mJcSwBthOuY4RHUS1wVPd4LQvxqhhCAMVtc66_QBxd1DBl6KsP8UEw"
        val response = menuRepository.findAllMenus(kioskId, accessToken)
    }
}