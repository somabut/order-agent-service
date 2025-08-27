package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.log.UtgEndLog
import com.orderagentservice.order.model.log.UtgStartLog
import com.orderagentservice.order.model.type.UtgType
import com.orderagentservice.order.service.MenuService
import com.orderagentservice.order.service.utg.menu.MenuUtgService
import com.orderagentservice.order.service.utg.payment.PaymentUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtgService @Autowired constructor(
    private val menuService: MenuService,
    private val menuGraphService: MenuUtgService,
    private val paymentUtgService: PaymentUtgService,
    private val logService: LogService,
    private val usageTracker: UsageTracker
) {
    @Transactional
    fun initializeGraph(kioskId: String, accessToken: String): List<AgentActionDto> {
        logService.printLog(
            UtgStartLog(
                kioskId = kioskId,
                utgType = UtgType.TOTAL
            )
        )
        val startTime = System.nanoTime()

        //관리자 페이지로부터 메뉴를 얻어온다
        val menuList = menuService.getMenus(kioskId, accessToken)

        val context = GraphContext.toBasicContext(kioskId)

        menuGraphService.initializeGraph(
            context = context,
            menuList = menuList
        )
        paymentUtgService.initializeGraph(context = context)

        val endTime = System.nanoTime()
        logService.printLog(
            UtgEndLog(
                kioskId = context.kioskId,
                utgType = UtgType.TOTAL,
                processingTime = (endTime - startTime) / 1000000,
                totalTokenUsage = usageTracker.totalUsage
            )
        )

        return context.history
    }

    @Transactional
    fun updateGraph(kioskId: String, editCategories: List<String>, accessToken: String) {
        val context = GraphContext.toBasicContext(kioskId)

        for (category in editCategories) {
            val menuList = menuService.getMenusByCategory(kioskId, category, accessToken)
            menuGraphService.updateGraph(context, menuList)
        }
    }
}