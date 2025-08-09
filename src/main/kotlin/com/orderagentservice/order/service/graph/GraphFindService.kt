package com.orderagentservice.order.service.graph

import com.orderagentservice.order.model.dto.ActionPathDto

interface GraphFindService {
    fun findPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto>
    fun findPaymentPath(kioskId: String, sourceId: String): List<ActionPathDto>
    fun findOption(kioskId: String, menuId: String, optKeyword: String): ActionPathDto
    fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto>
    fun findCategoryNodeId(kioskId: String, id: String): String
    fun findPlace(kioskId: String, id: String, place: String): ActionPathDto?
    fun findRoot(kioskId: String): ActionPathDto
    fun findStation(kioskId: String): ActionPathDto
}