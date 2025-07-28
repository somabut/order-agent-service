package com.orderagentservice.order.service

import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphInitializeContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtgInitializeService @Autowired constructor(
    private val menuService: MenuService,
    private val menuGraphInitializeService: MenuGraphInitializeService,
    private val paymentGraphInitializeService: PaymentGraphInitializeService
) {
    private val log = logger()

    @Transactional
    fun initializeGraph(kioskId: String, accessToken: String): List<AgentActionDto> {
        log.info("UTG 생성 시작")
        val startTime = System.nanoTime()

        //관리자 페이지로부터 메뉴를 얻어온다
        val menuList = menuService.getMenus(kioskId, accessToken)

        val context = GraphInitializeContext(
            kioskId = kioskId,
            determinePlace = false,
            lastNode = null,
            imageHash = null,
            nowCategory = null,
            history = mutableListOf<AgentActionDto>()
        )

        menuGraphInitializeService.initializeGraph(
            context = context,
            menuList = menuList
        )
        paymentGraphInitializeService.initializeGraph(context = context)

        val endTime = System.nanoTime()
        log.info("모든 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")

        return context.history
    }
}