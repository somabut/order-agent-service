package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.result.NodeCreationResult

interface MenuActionExecutor {
    fun selectCategory(context: UtgContext, menuDto: MenuInfoDto, uiList: List<UiComponentDto>): NodeCreationResult
    fun selectMenu(context: UtgContext, menuDto: MenuInfoDto, uiList: List<UiComponentDto>, categoryScreenId: String): String
    fun selectOption(context: UtgContext, menuDto: MenuInfoDto, menuNodeId: String, uiList: List<UiComponentDto>)
    fun selectBack(context: UtgContext, menuNodeId: String, uiList: List<UiComponentDto>): String
    fun selectModal(context: UtgContext, menuDto: MenuInfoDto, menuNodeId: String, uiList: List<UiComponentDto>): String
    fun cacheBackUi(context: UtgContext, uiList: List<UiComponentDto>): AgentBackDto
}