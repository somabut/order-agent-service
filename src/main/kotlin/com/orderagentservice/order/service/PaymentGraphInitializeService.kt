package com.orderagentservice.order.service

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PaymentGraphInitializeService @Autowired constructor(
    private val paymentAgent: PaymentAgent,
    private val notificationService: NotificationService,
    private val uiExtractorManager: UiExtractorManager,
    private val uiGraphService: UiGraphService
) {
    private val log = logger()

    fun initializeGraph(kioskId: String, lastNode: UiEntity): List<AgentActionDto> {
        log.info("결제 utg 생성 시작")
        val startTime = System.nanoTime()

        //존재하는 모든 메뉴에 대해 그래프를 그렸으니 결제까지 가는 노드를 만들어야함
        var isNext = true
        var llmUiList = mutableListOf<LlmUiComponentDto>()
        var preNode = lastNode
        val history = mutableListOf<AgentActionDto>()
        while (true) {
            val image = notificationService.sendCaptureCommand(kioskId)
            llmUiList = uiExtractorManager.getUiComponents(image, kioskId)
            val action = paymentAgent.determineAction(llmUiList)

            log.info("결제 노드를 생성합니다. go_next: ${action.goNext}, score: ${action.score}, coordinate: ${action.coordinate}, title: ${action.title}")
            isNext = action.goNext
            val entity = uiGraphService.saveNode(UiDto(
                isNext = isNext,
                x = action.coordinate[0], y = action.coordinate[1],
                title = action.title,
                kioskId = kioskId
            ))
            uiGraphService.saveRel(preNode.id, entity.id, NodeRelation.PATH_TO)

            history.add(action)

            preNode = entity
            if (isNext == false) break
        }

        val endTime = System.nanoTime()
        log.info("결제 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")

        return history
    }
}