package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.menu.MenuNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

interface MenuSelectStrategy {
    fun execute(context: UtgContext, menuDto: MenuInfoDto, uiList: List<UiComponentDto>, categoryScreenId: String): String
}

@Component
class DefaultMenuSelectStrategy @Autowired constructor(
    private val comparatorManager: ComparatorManager,
    private val notificationService: NotificationService,
    private val menuNodeIntegrator: MenuNodeIntegrator,
    private val screenNodeIntegrator: ScreenNodeIntegrator,
) : MenuSelectStrategy {
    override fun execute(
        context: UtgContext,
        menuDto: MenuInfoDto,
        uiList: List<UiComponentDto>,
        categoryScreenId: String
    ): String {
        val matchDto = comparatorManager.wordCompare(menuDto.title, uiList)

        //노드 생성
        val creationResult = menuNodeIntegrator.integrateMenuNode(matchDto, menuDto.title, context)
        val nodeId = creationResult.nodeId

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeIntegrator.linkNode(
            kioskId = context.kioskId,
            nodeId = nodeId, screenNodeId = categoryScreenId,
            uiComponentParams = creationResult.uiComponentParams,
        )

        //현재 메뉴 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(matchDto.x, matchDto.y, matchDto.title))

        return nodeId
    }
}