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
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl8wMDIiLCJpYXQiOjE3NTI1ODU1OTQsImV4cCI6MTc1MjY3MTk5NH0.WW5GTPDmd8sV6aECd_MbIytT4snwRJqw5Oy05Dg9jDMtfxRWmrf4_hA6e9XCD5Hh6RudlpqhK1lxPI3wRXdTKw"
        val kioskId = "kiosk-8f22eeb6-a920-44ed-af26-d800756fb283"
        val response = randomTaskService.generate(3, kioskId, accessToken)
        for (ele in response.autoOrderMenus) {
            println(ele)
            println(ele.autoOrderOptions)
        }
    }
}