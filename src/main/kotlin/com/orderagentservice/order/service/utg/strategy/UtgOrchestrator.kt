package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.exception.UtgInfiniteLoopException
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.log.UtgNowMenuLog
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.payment.PaymentNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UtgOrchestrator @Autowired constructor(
    private val utgActionFactory: UtgActionFactory,
    private val uiDetectorManager: UiDetectorManager,
    private val screenNodeIntegrator: ScreenNodeIntegrator,
    private val paymentNodeIntegrator: PaymentNodeIntegrator,
    private val graphService: UiGraphService,
    private val logService: LogService
) {
    private val log = logger()

    private val PAYMENT_MAX_LOOP = 5

    fun initialize(context: UtgContext, menuList: List<MenuInfoDto>, utgStrategyRequest: UtgStrategyRequest) {
        val actionProfile = utgActionFactory.createProfile(utgStrategyRequest)

        actionProfile.startSelectStrategy.execute(context)

        var uiList: List<UiComponentDto> = listOf()
        var categoryScreenId = context.screenNodeId
        for (menuDto in menuList) {
            val categoryNavigationResult = navigateCategory(
                context = context,
                menuDto = menuDto,
                actionProfile = actionProfile,
                uiList = uiList,
            )
            uiList = categoryNavigationResult.first
            categoryScreenId = categoryNavigationResult.second

            logService.printLog(
                UtgNowMenuLog(
                    kioskId = context.kioskId,
                    menu = menuDto.title,
                    category = menuDto.category
                )
            )

            navigateMenu(
                context = context,
                menuDto = menuDto,
                actionProfile = actionProfile,
                uiList = uiList,
                categoryScreenId = categoryScreenId
            )
        }

        //결제 노드 초기화
        if (navigatePayment(context = context, actionProfile = actionProfile) == false) {
            throw UtgInfiniteLoopException()
        }
    }

    private fun navigateCategory(
        context: UtgContext,
        menuDto: MenuInfoDto,
        actionProfile: UtgActionProfile,
        uiList: List<UiComponentDto>
    ): Pair<List<UiComponentDto>, String> {
        if (menuDto.category != context.currentCategory) {
            log.info("카테고리 이동: ${menuDto.category}")
            var currentUiList = uiDetectorManager.getUiComponents(context).uiElements

            val creationResult = actionProfile.categorySelectStrategy.execute(context, menuDto, currentUiList)

            screenNodeIntegrator.linkNode(
                kioskId = context.kioskId,
                nodeId = creationResult.nodeId,
                screenNodeId = context.screenNodeId,
                uiComponentParams = creationResult.uiComponentParams,
            )

            return Pair(uiDetectorManager.getUiComponents(context).uiElements, context.screenNodeId)
        }

        // 카테고리 이동이 필요 없는 경우 기존 상태를 그대로 반환
        return Pair(uiList, context.screenNodeId)
    }

    private fun navigateMenu(
        context: UtgContext,
        menuDto: MenuInfoDto,
        actionProfile: UtgActionProfile,
        uiList: List<UiComponentDto>,
        categoryScreenId: String
    ) {
        // 메뉴 선택
        var nodeId = actionProfile.menuSelectStrategy.execute(context, menuDto, uiList, categoryScreenId)
        val updatedUiList = uiDetectorManager.getUiComponents(context).uiElements

        // 옵션 처리 또는 모달 처리
        if (menuDto.options.isEmpty()) {
            logService.printLog(
                UtgProcessLog(
                    kioskId = context.kioskId,
                    message = "모달이 감지되어 모달을 처리합니다."
                )
            )
        } else {
            // 옵션이 있는 경우 처리
            actionProfile.optionSelectStrategy.execute(context, menuDto, nodeId, updatedUiList)
        }

        // 처리 후 뒤로가기 및 관계 저장
        nodeId = actionProfile.backSelectStrategy.execute(context, nodeId, updatedUiList)
        graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelationType.BACK_TO)
    }

    private fun navigatePayment(
        context: UtgContext,
        actionProfile: UtgActionProfile,
    ): Boolean {
        var loopTime = 0
        while (loopTime <= PAYMENT_MAX_LOOP) {
            val uiList = uiDetectorManager.getUiComponents(context).ocrElements

            val paymentEnd = actionProfile.paymentSelectStrategy.execute(context, uiList)
            if (paymentEnd == false) {
                paymentNodeIntegrator.integrateCompleteNode(context)
                return true
            }
            loopTime += 1
        }

        return false
    }
}