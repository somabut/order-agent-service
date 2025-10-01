package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.exception.UtgInfiniteLoopException
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.UtgNowMenuLog
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.service.utg.sequencer.CategoryActionSequencer
import com.orderagentservice.order.service.utg.sequencer.MenuActionSequencer
import com.orderagentservice.order.service.utg.sequencer.PaymentActionSequencer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UtgOrchestrator @Autowired constructor(
    private val utgActionFactory: UtgActionFactory,
    private val logService: LogService,
    private val categoryActionSequencer: CategoryActionSequencer,
    private val menuActionSequencer: MenuActionSequencer,
    private val paymentActionSequencer: PaymentActionSequencer
) {
    private val log = logger()

    private val PAYMENT_MAX_LOOP = 5

    fun execute(context: UtgContext, menuList: List<MenuInfoDto>, utgStrategyRequest: UtgStrategyRequest) {
        val actionProfile = utgActionFactory.createProfile(utgStrategyRequest)

        actionProfile.startSelectStrategy.execute(context)

        var uiList: List<UiComponentDto> = listOf()
        var categoryScreenId = context.screenNodeId
        for (menuDto in menuList) {
            val categoryNavigationResult = categoryActionSequencer.run(
                context = context,
                menuDto = menuDto,
                actionProfile = actionProfile,
                uiList = uiList,
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

        //결제 노드 초기화
        val paymentNavigateResult = paymentActionSequencer.run(context = context, actionProfile = actionProfile)
        if (paymentNavigateResult == false) {
            throw UtgInfiniteLoopException()
        }
    }
}