package com.orderagentservice.order.repository.ui

import com.orderagentservice.order.model.entity.UiEntity
import org.springframework.data.neo4j.repository.Neo4jRepository

interface UiGraphRepository : Neo4jRepository<UiEntity, String>,
    UiGraphFindRepository, UiGraphEditRepository, UiGraphDeleteRepository, UiGraphSaveRepository {

}