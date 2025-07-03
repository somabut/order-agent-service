package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.PaymentInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UtgInitializeService @Autowired constructor(
    private val menuAgent: MenuAgent,
    private val backAgent: BackAgent,
    private val paymentAgent: PaymentAgent,
    private val uiGraphService: UiGraphService,
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val menuGraphInitializeService: MenuGraphInitializeService,
    private val paymentGraphInitializeService: PaymentGraphInitializeService
) {
    private val log = logger()

    fun initializeGraph(
        url: String, kioskId: String,
        menuList: List<MenuInfoDto>, paymentList: List<PaymentInfoDto>
    ): List<AgentActionDto> {
        log.info("UTG 생성 시작")
        val startTime = System.nanoTime()

        val history = mutableListOf<AgentActionDto>()
        val result = menuGraphInitializeService.initializeGraph(url = url, kioskId = kioskId, menuList = menuList)
        val paymentHistory = paymentGraphInitializeService.initializeGraph(url, kioskId, result.second)

        for (ele in result.first) {
            history.add(ele)
        }
        for (ele in paymentHistory) {
            history.add(ele)
        }

        val endTime = System.nanoTime()
        log.info("모든 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")

        return history
    }
}