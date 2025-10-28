package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.log.UtgEndLog
import com.orderagentservice.order.model.log.UtgStartLog
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.LogicType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.model.type.UtgForLogType
import com.orderagentservice.order.service.MenuService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.menu.MenuUtgService
import com.orderagentservice.order.service.utg.payment.PaymentUtgService
import com.orderagentservice.order.service.utg.orchestrator.UtgInitializeOrchestrator
import com.orderagentservice.order.service.utg.orchestrator.UtgUpdateOrchestrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UtgService @Autowired constructor(
    private val menuService: MenuService,
    private val menuUtgService: MenuUtgService,
    private val paymentUtgService: PaymentUtgService,
    private val uiGraphService: UiGraphService,
    private val logService: LogService,
    private val usageTracker: UsageTracker,

    private val utgInitializeOrchestrator: UtgInitializeOrchestrator,
    private val utgUpdateOrchestrator: UtgUpdateOrchestrator
) {
    private val log = logger()

//    fun initializeGraph(kioskId: String, accessToken: String): List<AgentActionDto> {
//        logService.printLog(
//            UtgStartLog(
//                kioskId = kioskId,
//                utgForLogType = UtgForLogType.TOTAL
//            )
//        )
//        val startTime = System.nanoTime()
//
//        //관리자 페이지로부터 메뉴를 얻어온다
//        val menuList = menuService.getMenus(kioskId, accessToken)
//
//        val context = UtgContext.toBasicContext(kioskId)
//
//        menuUtgService.initializeGraph(
//            context = context,
//            menuList = menuList
//        )
//        paymentUtgService.initializeGraph(context = context)
//
//        val endTime = System.nanoTime()
//        logService.printLog(
//            UtgEndLog(
//                kioskId = context.kioskId,
//                utgForLogType = UtgForLogType.TOTAL,
//                processingTime = (endTime - startTime) / 1000000,
//                totalTokenUsage = usageTracker.totalUsage
//            )
//        )
//
//        return context.history
//    }

    fun init(kioskId: String, accessToken: String, utgStrategyRequest: UtgStrategyRequest): List<AgentActionDto> {
        logService.printLog(
            UtgStartLog(
                kioskId = kioskId,
                utgForLogType = UtgForLogType.TOTAL
            )
        )
        val startTime = System.nanoTime()

        //관리자 페이지로부터 메뉴를 얻어온다
        val menuList = menuService.getMenus(kioskId, accessToken)

        val context = UtgContext.toBasicContext(kioskId)
        utgInitializeOrchestrator.execute(context, menuList, utgStrategyRequest)

        val endTime = System.nanoTime()
        logService.printLog(
            UtgEndLog(
                kioskId = context.kioskId,
                utgForLogType = UtgForLogType.TOTAL,
                processingTime = (endTime - startTime) / 1000000,
                totalTokenUsage = usageTracker.totalUsage
            )
        )

        return context.history
    }

    fun updateCategory(
        kioskId: String, accessToken: String,
        isInitPayment: Boolean
    ): List<String> {
        val context = UtgContext.toBasicContext(kioskId, LogicType.UPDATE)
        val menuList = menuService.getMenus(kioskId, accessToken)

        //수정된 카테고리까지 가서 메뉴 노드 그리기
        log.info("카테고리를 수정합니다")
        val uiDtoList = uiGraphService.findModified(context.kioskId)

        val modifiedCategoryList = uiDtoList
            .filter { it.type == NodeType.CATEGORY }
            .map { it.title }
        val pendingList = menuList.filter { it.category in modifiedCategoryList }

        utgUpdateOrchestrator.editCategories(
            context = context,
            modifiedCategoryList = modifiedCategoryList, pendingList = pendingList, menuList = menuList,
            isInitPayment = isInitPayment
        )

        return context.pushedImages
    }

    fun updateMenu(
        kioskId: String, accessToken: String,
        isInitPayment: Boolean
    ): List<String> {
        val context = UtgContext.toBasicContext(kioskId, LogicType.UPDATE)
        val menuList = menuService.getMenus(kioskId, accessToken)

        //수정된 메뉴까지 가서 옵션 노드 그리기
        log.info("메뉴를 수정합니다")
        val uiDtoList = uiGraphService.findModified(context.kioskId)

        val modifiedMenuList = uiDtoList
            .filter { it.type == NodeType.MENU }
            .map { it.title }
        val pendingList = menuList.filter { it.title in modifiedMenuList }

        utgUpdateOrchestrator.editMenus(
            context = context,
            pendingList = pendingList, menuList = menuList, modifiedMenuList = modifiedMenuList,
            isInitPayment = isInitPayment
        )

        return context.pushedImages
    }

    fun updatePayment(kioskId: String, accessToken: String): List<String> {
        val context = UtgContext.toBasicContext(kioskId, LogicType.UPDATE)
        val randomMenu = menuService.getMenus(kioskId, accessToken)[0]

        val uiDtoList = uiGraphService.findModified(context.kioskId)
        val modifiedPayment = uiDtoList
            .filter { it.type == NodeType.PAYMENT }
            .map { it.title }
            .first()

        utgUpdateOrchestrator.editPayment(context, modifiedPayment, randomMenu)
        uiGraphService.changeModified(context.kioskId, modifiedPayment)

        return context.pushedImages
    }

    fun updateCategoryGraph(
        kioskId: String, accessToken: String,
        isInitPayment: Boolean
    ): List<String> {
        val context = UtgContext.toBasicContext(kioskId, LogicType.UPDATE)

        //수정된 메뉴, 완료된 메뉴 가져오기
        val menuList = menuService.getMenus(kioskId, accessToken)

        menuUtgService.updateCategory(context, menuList)
        if (isInitPayment) {
            paymentUtgService.initializeGraph(context)
        }

        return context.pushedImages
    }

    fun updateMenuGraph(
        kioskId: String, accessToken: String,
        isInitPayment: Boolean
    ): List<String> {
        val context = UtgContext.toBasicContext(kioskId, LogicType.UPDATE)

        //수정된 메뉴, 완료된 메뉴 가져오기
        val menuList = menuService.getMenus(kioskId, accessToken)

        menuUtgService.updateMenu(context, menuList)
        if (isInitPayment) {
            paymentUtgService.initializeGraph(context)
        }

        return context.pushedImages
    }

    fun updatePaymentGraph(kioskId: String): List<String> {
        val context = UtgContext.toBasicContext(kioskId, LogicType.UPDATE)
        paymentUtgService.updatePayment(context)

        return context.pushedImages
    }
}