package com.orderagentservice.order.service.utg.orchestrator

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.exception.UtgInfiniteLoopException
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.UtgNowMenuLog
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.sequencer.CategoryActionSequencer
import com.orderagentservice.order.service.utg.sequencer.MenuActionSequencer
import com.orderagentservice.order.service.utg.sequencer.PaymentActionSequencer
import com.orderagentservice.order.service.utg.UtgActionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UtgInitializeOrchestrator @Autowired constructor(
    private val utgActionFactory: UtgActionFactory,
    private val categoryActionSequencer: CategoryActionSequencer,
    private val menuActionSequencer: MenuActionSequencer,
    private val paymentActionSequencer: PaymentActionSequencer,
    private val uiDetectorManager: UiDetectorManager,
    private val logService: LogService,
) {
    fun execute(context: UtgContext, menuList: List<MenuInfoDto>, utgStrategyRequest: UtgStrategyRequest) {
        val actionProfile = utgActionFactory.createProfile(utgStrategyRequest)

        //root, station, 첫 매장/포장 선택등을 진행
        actionProfile.startSelectStrategy.execute(context, utgStrategyRequest)

        //메뉴 노드 초기화
        navigateMenus(context, menuList, actionProfile)

        //결제 노드 초기화
        context.lastNodeId = context.stationNodeId
        navigatePayment(context, actionProfile)
    }

    fun navigateMenus(context: UtgContext, menuList: List<MenuInfoDto>, actionProfile: UtgActionProfile) {
        var uiList: List<UiComponentDto> = uiDetectorManager.getUiComponents(context).uiElements
        var categoryScreenId = context.screenNodeId
        for (menuDto in menuList) {
            val categoryNavigationResult = categoryActionSequencer.run(
                context = context,
                menuDto = menuDto,
                actionProfile = actionProfile,
                uiList = uiList,
                originCategoryScreenId = categoryScreenId
            )

            uiList = categoryNavigationResult.uiList
            categoryScreenId = categoryNavigationResult.categoryScreenId

            logService.printLog(
                UtgNowMenuLog(
                    kioskId = context.kioskId,
                    menu = menuDto.title,
                    category = menuDto.category
                )
            )

            menuActionSequencer.run(
                context = context,
                menuDto = menuDto,
                actionProfile = actionProfile,
                uiList = uiList,
                categoryScreenId = categoryScreenId
            )
        }
    }

    fun navigatePayment(context: UtgContext, actionProfile: UtgActionProfile) {
        val paymentNavigateResult = paymentActionSequencer.run(context = context, actionProfile = actionProfile)
        if (paymentNavigateResult == false) {
            throw UtgInfiniteLoopException()
        }
    }
}