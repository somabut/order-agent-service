package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.service.MenuService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtgService @Autowired constructor(
    private val menuService: MenuService,
    private val menuGraphService: MenuUtgService,
    private val paymentUtgService: PaymentUtgService
) {
    private val log = logger()

    @Transactional
    fun initializeGraph(kioskId: String, accessToken: String): List<AgentActionDto> {
        log.info("UTG 생성 시작")
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
        log.info("모든 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")

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