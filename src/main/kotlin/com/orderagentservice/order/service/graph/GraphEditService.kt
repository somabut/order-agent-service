package com.orderagentservice.order.service.graph

interface GraphEditService {
    fun changeTitle(nodeId: String, kioskId: String, title: String)
}