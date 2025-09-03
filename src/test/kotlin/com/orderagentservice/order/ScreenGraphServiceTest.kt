package com.orderagentservice.order

import com.orderagentservice.order.model.dto.ScreenDto
import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.service.graph.screen.ScreenGraphService
import com.orderagentservice.order.service.graph.som.SomGraphService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ScreenGraphServiceTest @Autowired constructor(
    private val screenGraphService: ScreenGraphService
){
    @Test
    fun `screen 노드가 저장된다`() {
        val entity = screenGraphService.saveNode(ScreenDto(
            kioskId = "kiosk",
            imageUrl = "test"
        ))
        print(entity)
    }
}