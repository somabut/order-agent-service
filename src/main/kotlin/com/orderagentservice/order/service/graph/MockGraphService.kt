package com.orderagentservice.order.service.graph

import com.orderagentservice.order.model.type.NodeRelationType
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
            x = uiDto.x, y = uiDto.y, isNext = uiDto.isNext,
            title = uiDto.title, kioskId = uiDto.kioskId
        )
        return uiEntity
    }

    override fun findNodeByTitle(kioskId: String, title: String) = ""

    override fun isBackRel(kioskId: String, sourceId: String) = false

    override fun saveRel(sourceId: String, targetId: String, type: NodeRelationType) {}

    override fun changeTitle(nodeId: String, kioskId: String, title: String) {}

    override fun findPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto> = listOf()

    override fun findPaymentPath(kioskId: String, sourceId: String): List<ActionPathDto> = listOf()

    override fun findOption(kioskId: String, menuId: String, optKeyword: String): ActionPathDto = ActionPathDto(id = "", title = "", x = -1, y = -1)

    override fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto> = listOf()

    override fun findCategoryNodeId(kioskId: String, id: String): String = ""

    override fun findPlace(kioskId: String, id: String, place: String): ActionPathDto? = ActionPathDto(id = "", title = "", x = -1, y = -1)

    override fun findRoot(kioskId: String): ActionPathDto = ActionPathDto(id = "", title = "", x = -1, y = -1)

    override fun findStation(kioskId: String): ActionPathDto = ActionPathDto(id = "", title = "", x = -1, y = -1)

    override fun deleteMenusByCategory(kioskId: String, id: String) {}
}