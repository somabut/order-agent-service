package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.type.StrategyType
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.menu.MenuNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

interface OptionSelectStrategy {
    fun execute(context: UtgContext, menuDto: MenuInfoDto, menuNodeId: String, uiList: List<UiComponentDto>)
}

@Component(StrategyType.IN_OPTION)
class DefaultOptionSelectStrategy @Autowired constructor(
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
    }
}

@Component(StrategyType.EX_OPTION)
class NoneOptionSelectStrategy : OptionSelectStrategy {
    override fun execute(context: UtgContext, menuDto: MenuInfoDto, menuNodeId: String, uiList: List<UiComponentDto>) {

    }
}