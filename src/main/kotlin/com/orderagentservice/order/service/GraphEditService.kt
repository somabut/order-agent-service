package com.orderagentservice.order.service

interface GraphEditService {
    fun changeTitle(nodeId: String, kioskId: String, title: String)
}