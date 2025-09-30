package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.collections.component1
import kotlin.collections.component2

interface OptionSelectStrategy {
    fun execute(context: UtgContext, menuDto: MenuInfoDto, menuNodeId: String, uiList: List<UiComponentDto>)
}

@Component
class BackOptionSelectStrategy @Autowired constructor(
    private val comparatorManager: ComparatorManager,
    private val screenNodeIntegrator: ScreenNodeIntegrator,
    private val menuNodeIntegrator: MenuNodeIntegrator,
) : OptionSelectStrategy {
    override fun execute(
        context: UtgContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ) {
        //메뉴의 옵션 노드 추가
        for (opt in menuDto.options) {
            val matchDto = comparatorManager.wordCompare(opt, uiList)

            //노드 생성
            val creationResult = menuNodeIntegrator.integrateOptionNode(matchDto, opt, menuNodeId, context)

            //match 노드와 관계, screen 노드와 관계 연결
            screenNodeIntegrator.linkNode(
                kioskId = context.kioskId,
                nodeId = creationResult.nodeId, screenNodeId = context.screenNodeId,
                uiComponentParams = creationResult.uiComponentParams,
            )
        }

        //TODO(back UI선택 해야함)
    }
}