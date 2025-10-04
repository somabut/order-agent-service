package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.log.UtgEndLog
import com.orderagentservice.order.model.log.UtgStartLog
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.LogicType
import com.orderagentservice.order.model.type.UtgForLogType
import com.orderagentservice.order.service.MenuService
import com.orderagentservice.order.service.utg.menu.MenuUtgService
import com.orderagentservice.order.service.utg.payment.PaymentUtgService
import com.orderagentservice.order.service.utg.strategy.UtgInitializeOrchestrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UtgService @Autowired constructor(
    private val menuService: MenuService,
    private val menuUtgService: MenuUtgService,
    private val paymentUtgService: PaymentUtgService,
    private val logService: LogService,
    private val usageTracker: UsageTracker,

    private val utgInitializeOrchestrator: UtgInitializeOrchestrator
) {
    fun initializeGraph(kioskId: String, accessToken: String): List<AgentActionDto> {
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

        menuUtgService.initializeGraph(
            context = context,
            menuList = menuList
        )
        paymentUtgService.initializeGraph(context = context)

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