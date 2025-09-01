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
                minX = -1, minY = -1, maxX = -1, maxY = -1,
                content = "test"
            )
        )
    }
}