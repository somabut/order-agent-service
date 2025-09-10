package com.orderagentservice.order.service.graph.ui

import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.UiDto

interface UiGraphFindService {
    fun findNodeByTitle(kioskId: String, title: String): String
    fun findPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto>
    fun findPaymentPath(kioskId: String, sourceId: String): List<ActionPathDto>
    fun findOption(kioskId: String, menuId: String, optKeyword: String): ActionPathDto
    fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto>
    fun findCategoryNodeId(kioskId: String, id: String): String
    fun findPlace(kioskId: String, id: String, place: String): ActionPathDto?
    fun findRoot(kioskId: String): ActionPathDto
    fun findStation(kioskId: String): ActionPathDto
    fun findModified(kioskId: String): List<UiDto>
    fun findAll(kioskId: String): List<UiDto>

    fun isBackRel(kioskId: String, sourceId: String): Boolean
}