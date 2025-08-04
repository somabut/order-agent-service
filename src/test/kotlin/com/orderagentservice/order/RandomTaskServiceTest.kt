package com.orderagentservice.order

import com.orderagentservice.order.service.RandomTaskService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RandomTaskServiceTest @Autowired constructor(
    private val randomTaskService: RandomTaskService
) {
    @Test
    fun `adfafsd`() {
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2UxMjMiLCJpYXQiOjE3NTQyODkzNDksImV4cCI6MTc1NDM3NTc0OX0.CW0Mfd_HNK2nij_Mdleimj8YeJbiFXvWy-EciL6fxDHNkChcriEdT2sHlrlhhoMrJyxN3gB9mGNGod60i_Foxg"
        val kioskId = "kiosk-d89e07fa-4361-4b6a-a550-ac580a1ba195"
        val response = randomTaskService.generate(5, kioskId, accessToken)
        for (ele in response.autoOrderMenus) {
            println(ele)
            println(ele.autoOrderOptions)
        }
    }
}