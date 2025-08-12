package com.orderagentservice.order.service

interface GraphDeleteService {
    fun deleteMenusByCategory(kioskId: String, id: String)
}