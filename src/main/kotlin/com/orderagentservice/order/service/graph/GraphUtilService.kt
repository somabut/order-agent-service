package com.orderagentservice.order.service.graph

interface GraphUtilService {
    fun changeTitle(nodeId: String, kioskId: String, title: String)
    fun deleteMenusByCategory(kioskId: String, id: String)
}