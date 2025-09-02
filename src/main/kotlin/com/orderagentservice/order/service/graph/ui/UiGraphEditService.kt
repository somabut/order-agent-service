package com.orderagentservice.order.service.graph.ui

interface UiGraphEditService {
    fun changeTitle(nodeId: String, kioskId: String, title: String)
}