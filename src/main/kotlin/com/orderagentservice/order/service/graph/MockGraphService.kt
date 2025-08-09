package com.orderagentservice.order.service.graph

import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("test")
@Service
class MockGraphService : GraphService {
    override fun saveNode(uiDto: UiDto): UiEntity {
        val uiEntity = UiEntity(
            x = -1, y = -1, isNext = false,
            title = "TEST", kioskId = "TEST"
        )
        return uiEntity
    }

    override fun saveRel(sourceId: String, targetId: String, type: NodeRelation) {

    }

    override fun changeTitle(nodeId: String, kioskId: String, title: String) {

    }

    override fun findPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto> {
        TODO("Not yet implemented")
    }

    override fun findPaymentPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        TODO("Not yet implemented")
    }

    override fun findOption(kioskId: String, menuId: String, optKeyword: String): ActionPathDto {
        TODO("Not yet implemented")
    }

    override fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        TODO("Not yet implemented")
    }

    override fun findCategoryNodeId(kioskId: String, id: String): String {
        TODO("Not yet implemented")
    }

    override fun findPlace(kioskId: String, id: String, place: String): ActionPathDto? {
        TODO("Not yet implemented")
    }

    override fun findRoot(kioskId: String): ActionPathDto {
        TODO("Not yet implemented")
    }

    override fun findStation(kioskId: String): ActionPathDto {
        TODO("Not yet implemented")
    }

    override fun deleteMenusByCategory(kioskId: String, id: String) {
        TODO("Not yet implemented")
    }
}