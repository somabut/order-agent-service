package com.orderagentservice.order

import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.service.graph.som.SomGraphService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SomGraphServiceTest @Autowired constructor(
    private val somGraphService: SomGraphService,
){
    @Test
    fun `som 노드가 저장된다`() {
        somGraphService.saveNode(
            SomDto(
                kioskId = "kiosk",
                minX = -1, minY = -1, maxX = -1, maxY = -1,
                content = "test"
            )
        )
    }

    @Test
    fun `매칭된 screen에 관계된 som을 찾는다`() {
        val sourceId = "7a75f069-8c94-4b81-889f-3b8f34883788"
        val kioskId = "kiosk-2303452c-8454-4b9f-add2-47314cfd3911"
        val nodeId = somGraphService.findNode(
            sourceId = sourceId,
            kioskId = kioskId,
            minX = 600, minY = 398, maxX = 744, maxY = 432,
            title = "녹차빽스치노"
        )
        println(nodeId)
    }
}