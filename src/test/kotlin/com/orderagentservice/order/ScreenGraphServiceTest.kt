package com.orderagentservice.order

import com.orderagentservice.order.model.dto.ScreenDto
import com.orderagentservice.order.model.dto.SomDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.graph.screen.ScreenGraphService
import com.orderagentservice.order.service.graph.som.SomGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ScreenGraphServiceTest @Autowired constructor(
    private val screenGraphService: ScreenGraphService,
    private val somGraphService: SomGraphService,
    private val uiGraphService: UiGraphService
){
    @Test
    fun `screen 노드가 저장된다`() {
        val entity = screenGraphService.saveNode(ScreenDto(
            kioskId = "kiosk",
            imageUrl = "test"
        ))
        print(entity)
    }

    @Test
    fun `관계가 설정된다`() {
        val nodeId = uiGraphService.saveNode(
            UiDto(
                isNext = false,
                x = -1, y = -1, title = "",
                kioskId = "kiosk",
            )
        ).id
        val screenNodeId = screenGraphService.saveNode(ScreenDto(
            kioskId = "kiosk",
            imageUrl = "test"
        )).id
        val somNodeId = somGraphService.saveNode(
            SomDto(
            kioskId = "kiosk",
            minX = -1, minY = -1, maxX = -1, maxY = -1,
            content = "test"
        )
        )

        uiGraphService.saveRel(nodeId, somNodeId, NodeRelationType.MATCH_TO)
        uiGraphService.saveRel(nodeId, screenNodeId, NodeRelationType.IMAGE_TO)
    }
}