package com.orderagentservice.order.service

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentGraphService @Autowired constructor(
    private val paymentAgent: PaymentAgent,
    private val placeGraphService: PlaceGraphService,
    private val notificationService: NotificationService,
    private val uiExtractorManager: UiExtractorManager,
    private val utgDataService: UtgDataService
) {
    private val log = logger()

    @Transactional
    fun initializeGraph(context: GraphContext) {
        log.info("결제 utg 생성 시작")
        val startTime = System.nanoTime()

        processPayments(context)
        createCompleteNode(context)

        val endTime = System.nanoTime()
        log.info("결제 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    private fun processPayments(context: GraphContext) {
        while (true) {
            //포장/매장 UI 확인
            if (context.isPlaceDetermined == false) {
                placeGraphService.initializeGraph(context)
            }
            val paymentEnd = selectPayments(context)
            if (paymentEnd == false) break
        }
    }

    private fun selectPayments(context: GraphContext): Boolean {
        val llmUiList = uiExtractorManager.getUiComponents(context.kioskId)
        val action = paymentAgent.determineAction(llmUiList)

        //노드 저장
        createPaymentNode(action, context)

        //클릭 액션 요청
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(x = action.coordinate[0], y = action.coordinate[1], title = action.title))

        return action.goNext
    }

    private fun createPaymentNode(action: AgentActionDto, context: GraphContext) {
        log.info("결제 노드를 생성합니다. go_next: ${action.goNext}, score: ${action.score}, coordinate: ${action.coordinate}, title: ${action.title}")

        val entity = utgDataService.saveNode(UiDto(
            isNext = action.goNext,
            x = action.coordinate[0], y = action.coordinate[1],
            title = action.title,
            kioskId = context.kioskId
        ))
        utgDataService.saveRel(context.lastNodeId!!, entity.id, NodeRelation.PATH_TO)

        context.lastNodeId = entity.id
    }

    private fun createCompleteNode(context: GraphContext) {
        log.info("완료 노드를 생성합니다.")

        val completeEntity =  utgDataService.saveNode(UiDto(
            isNext = false,
            x = -1, y = -1,
            title = "complete",
            kioskId = context.kioskId
        ))
        utgDataService.saveRel(context.lastNodeId!!, completeEntity.id, NodeRelation.PATH_TO)
    }
}