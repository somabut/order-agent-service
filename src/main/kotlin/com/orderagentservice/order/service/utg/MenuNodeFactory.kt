package com.orderagentservice.order.service.utg

import com.orderagentservice.order.service.graph.GraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuNodeFactory @Autowired constructor(
    private val graphService: GraphService
) {

}