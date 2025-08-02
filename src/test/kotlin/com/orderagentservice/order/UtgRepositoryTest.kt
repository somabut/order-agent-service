package com.orderagentservice.order

import com.orderagentservice.order.model.dto.UiDto
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
        val nowNode = utgService.findRootNode(kioskId)

        val path = utgService.findMenuPath(kioskId, nowNode.id, "불끈버거 맥시멈")
        println(path)
    }

    @Test
    fun `노드를 저장한다`() {
        val kioskId = "test"
        val entity = utgService.saveNode(
            UiDto(
                isNext = false,
                x = -1, y = -1,
                title = "test", kioskId = kioskId
            )
        )
        println(entity)
    }
}