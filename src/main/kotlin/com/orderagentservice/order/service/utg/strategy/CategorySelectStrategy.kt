package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.result.NodeCreationResult
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.menu.MenuNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

interface CategorySelectStrategy {
    fun execute(context: UtgContext, menuDto: MenuInfoDto, uiList: List<UiComponentDto>): NodeCreationResult
}

@Component
class DefaultCategorySelectStrategy @Autowired constructor(
    private val comparatorManager: ComparatorManager,
    private val notificationService: NotificationService,
    private val menuNodeIntegrator: MenuNodeIntegrator,
) : CategorySelectStrategy {
    override fun execute(
        context: UtgContext,
        menuDto: MenuInfoDto,
        uiList: List<UiComponentDto>
    ): NodeCreationResult {
        val matchDto = comparatorManager.wordCompare(menuDto.category, uiList)

        //노드 생성
        val nodeCreationResult = menuNodeIntegrator.integrateCategoryNode(matchDto, menuDto.category, context)
        context.lastNodeId = nodeCreationResult.nodeId

        //현재 카테고리 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(matchDto.x, matchDto.y, matchDto.title))

        return nodeCreationResult
    }
}