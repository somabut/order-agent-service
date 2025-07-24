package com.orderagentservice.order

import com.orderagentservice.order.repository.UtgRepository
import com.orderagentservice.order.service.UtgService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UtgRepositoryTest @Autowired constructor(
    private val utgService: UtgService,
    private val utgRepository: UtgRepository
) {
    @Test
    fun `원하는 메뉴의 경로를 찾는다`() {
        val kioskId = "kiosk-8f22eeb6-a920-44ed-af26-d800756fb283"
        val nowNode = utgService.findRootNodeId(kioskId)

        val path = utgService.findMenuPath(kioskId, nowNode.id, "complete")
        println(path)
    }
}