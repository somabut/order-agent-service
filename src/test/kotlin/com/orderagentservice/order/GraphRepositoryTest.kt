package com.orderagentservice.order

import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.repository.GraphRepository
import com.orderagentservice.order.service.GraphService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GraphRepositoryTest @Autowired constructor(
    private val graphService: GraphService,
    private val graphRepository: GraphRepository
) {
    @Test
    fun `원하는 메뉴의 경로를 찾는다`() {
        val kioskId = "kiosk-8f22eeb6-a920-44ed-af26-d800756fb283"
        val nowNode = graphService.findRoot(kioskId)

        val path = graphService.findMenuPath(kioskId, nowNode.id, "불끈버거 맥시멈")
        println(path)
    }

    @Test
    fun `노드를 저장한다`() {
        val kioskId = "test"
        val entity = graphService.saveNode(
            UiDto(
                isNext = false,
                x = -1, y = -1,
                title = "test", kioskId = kioskId
            )
        )
        println(entity)
    }
}