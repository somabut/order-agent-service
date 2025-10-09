package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.type.StrategyType
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.menu.UiNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

abstract class OptionSelectStrategy {
    protected abstract val comparatorManager: ComparatorManager
    protected abstract  val screenNodeIntegrator: ScreenNodeIntegrator
    protected abstract  val uiNodeIntegrator: UiNodeIntegrator

    abstract fun execute(context: UtgContext, menuDto: MenuInfoDto, menuNodeId: String, uiList: List<UiComponentDto>): List<UiComponentDto>

    protected fun navigateOption(
        context: UtgContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): List<UiComponentDto> {
        //메뉴의 옵션 노드 추가
        for (opt in menuDto.options) {
            val matchDto = comparatorManager.wordCompare(opt, uiList)

            //노드 생성
            val creationResult = uiNodeIntegrator.integrateOptionNode(matchDto, opt, menuNodeId, context)

            //match 노드와 관계, screen 노드와 관계 연결
            screenNodeIntegrator.linkNode(
                kioskId = context.kioskId,
                nodeId = creationResult.nodeId, screenNodeId = context.screenNodeId,
                uiComponentParams = creationResult.uiComponentParams,
            )
        }

        return uiList
    }
}

@Component(StrategyType.IN_OPTION)
class DefaultOptionSelectStrategy @Autowired constructor(
    override val comparatorManager: ComparatorManager,
    override val screenNodeIntegrator: ScreenNodeIntegrator,
    override val uiNodeIntegrator: UiNodeIntegrator,

) : OptionSelectStrategy() {
    override fun execute(
        context: UtgContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): List<UiComponentDto> {
        return navigateOption(
            context = context,
            menuDto = menuDto,
            menuNodeId = menuNodeId,
            uiList = uiList
        )
    }
}

@Component(StrategyType.EX_OPTION)
class NoneOptionSelectStrategy @Autowired constructor(
    override val comparatorManager: ComparatorManager,
    override val screenNodeIntegrator: ScreenNodeIntegrator,
    override val uiNodeIntegrator: UiNodeIntegrator,
    private val uiDetectorManager: UiDetectorManager
) : OptionSelectStrategy() {
    override fun execute(
        context: UtgContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): List<UiComponentDto> {
        val updatedUiList = uiDetectorManager.getUiComponents(context).uiElements
        return navigateOption(
            context = context,
            menuDto = menuDto,
            menuNodeId = menuNodeId,
            uiList = updatedUiList
        )
    }
}