package com.orderagentservice.order

import com.orderagentservice.order.model.dto.InfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.graph.info.InfoGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.strategy.OptionSelectStrategy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InfoGraphServiceTest @Autowired constructor(
    private val infoGraphService: InfoGraphService,
    private val uiGraphService: UiGraphService,
){
    @Test
    fun `info 노드가 저장된다`() {
        val entity = infoGraphService.saveNode(
            InfoDto(
                startStrategy = "TEST",
                optionStrategy = "TEST",
                backStrategy = "TEST",
                paymentStrategy = "TEST",
            )
        )
        println(entity)
    }

    @Test
    fun `root 노드와 관계를 맺는다`() {
        val kioskId = "test"
        val rootEntity = uiGraphService.saveNode(
            UiDto(
                isNext = false, kioskId = kioskId,
                x = -1, y = -1, title = "root", type = NodeType.ROOT
            )
        )
        val infoEntity = infoGraphService.saveNode(
            InfoDto(
                startStrategy = "TEST",
                optionStrategy = "TEST",
                backStrategy = "TEST",
                paymentStrategy = "TEST",
            )
        )
        println(infoEntity.id)
        infoGraphService.saveRel(kioskId, infoEntity.id)
    }

    @Test
    fun `info노드를 가져온다`() {
        val kioskId = "kiosk-85da7a5b-64fa-461f-9039-c6586ba7e048"
        val request = infoGraphService.findLinkedInfo(kioskId)
        println(request)
    }
}