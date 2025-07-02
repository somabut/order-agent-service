package com.orderagentservice.order

import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.repository.UiRepository
import com.orderagentservice.order.service.UiGraphService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UiGraphServiceTest @Autowired constructor(
    private val uiGraphService: UiGraphService,
    private val uiRepository: UiRepository
) {
    @Test
    fun `neo4j에 노드를 저장한다`() {
        //then: UI 노드가 주어진다
        val uiDto = UiDto(
            isNext = true,
            x = -1,
            y = -1,
            url = "url",
            title = "root"
        )

        //when: UI 노드를 저장한다.
        uiGraphService.saveNode(uiDto)

        //then: 저장이 완료된다.
    }

    @Test
    fun `neo4j에 경로를 저장한다`() {
        //then: UI 노드가 주어진다
        val uiDto1 = UiDto(
            isNext = true,
            x = -1,
            y = -1,
            url = "url",
            title = "root1"
        )
        val uiDto2 = UiDto(
            isNext = true,
            x = -1,
            y = -1,
            url = "url",
            title = "root2"
        )

        //when: UI 노드를 저장하고 관계를 맺는다
        val entity1 = uiGraphService.saveNode(uiDto1)
        val entity2 = uiGraphService.saveNode(uiDto2)
        uiGraphService.saveRel(entity1.id, entity2.id, NodeRelation.PATH_TO)

        //then: 저장이 완료된다
    }

    @Test
    fun `저장된 노드에서 특정 노드까지 경로를 찾는다`() {
        val url = "https://kiosk-web-sooty.vercel.app/"
        val input = "핫초코"
        val path = uiGraphService.findTargetPath(url, "주문")
        println(path)
    }
}