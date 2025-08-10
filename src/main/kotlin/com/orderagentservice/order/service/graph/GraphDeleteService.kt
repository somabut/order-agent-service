package com.orderagentservice.order.service.graph

interface GraphDeleteService {
    fun deleteMenusByCategory(kioskId: String, id: String)
}