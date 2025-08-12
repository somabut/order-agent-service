package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.MenuInfoDto

interface MenuActionExecutor {
    fun selectCategory(context: GraphContext, menuDto: MenuInfoDto, uiList: List<UiComponentDto>)
    fun selectMenu(context: GraphContext, menuDto: MenuInfoDto, uiList: List<UiComponentDto>): String
    fun selectOption(context: GraphContext, menuDto: MenuInfoDto, menuNodeId: String)
    fun selectBack(context: GraphContext, menuNodeId: String, uiList: List<UiComponentDto>): String
    fun selectModal(context: GraphContext, menuDto: MenuInfoDto, menuNodeId: String, uiList: List<UiComponentDto>): String
}