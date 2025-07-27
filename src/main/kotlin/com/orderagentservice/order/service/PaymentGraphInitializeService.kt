package com.orderagentservice.order.service

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.exception.LowScoreException
import com.orderagentservice.order.model.GraphInitializeContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentGraphInitializeService @Autowired constructor(
    private val paymentAgent: PaymentAgent,
    private val placeGraphInitializeService: PlaceGraphInitializeService,
    private val notificationService: NotificationService,
    private val uiExtractorManager: UiExtractorManager,
    private val utgService: UtgService
) {
    private val log = logger()

    @Transactional
    fun initializeGraph(context: GraphInitializeContext) {
        log.info("결제 utg 생성 시작")
        val startTime = System.nanoTime()
        val kioskId = context.kioskId

        //존재하는 모든 메뉴에 대해 그래프를 그렸으니 결제까지 가는 노드를 만들어야함
        var isNext = true
        var llmUiList = mutableListOf<LlmUiComponentDto>()
        while (true) {
            val image = notificationService.sendCaptureCommand(kioskId)
            llmUiList = uiExtractorManager.getUiComponents(image, kioskId)

            //포장/매장 UI 확인
            if (context.determinePlace == false) {
                placeGraphInitializeService.initializeGraph(context, llmUiList)
            }

            val action = paymentAgent.determineAction(llmUiList)
            context.history.add(action)

            val addCount = when {
                action.score in 0.6..<0.7 -> 1
                action.score <= 0.5 -> 3
                else -> 0
            }
            if (addCount > 0) {
                log.info("낮은 액션 정확도 점수: ${action.score}")
                context.lowScoreCount += addCount
                continue
            }

            //낮은 점수가 쌓이면 예외
            if (context.lowScoreCount >= 5) {
                throw LowScoreException()
            }

            //클릭 액션 요청
            notificationService.sendActionCommand(kioskId, CoordinateDto(x = action.coordinate[0], y = action.coordinate[1], title = action.title))

            log.info("결제 노드를 생성합니다. go_next: ${action.goNext}, score: ${action.score}, coordinate: ${action.coordinate}, title: ${action.title}")
            isNext = action.goNext
            val entity = utgService.saveNode(UiDto(
                isNext = isNext,
                x = action.coordinate[0], y = action.coordinate[1],
                title = action.title,
                kioskId = kioskId
            ))
            utgService.saveRel(context.lastNode!!.id, entity.id, NodeRelation.PATH_TO)

            context.lastNode = entity
            if (isNext == false) break
        }

        //완료 노드를 추가하여 UTG의 끝을 알리기
        val completeEntity =  utgService.saveNode(UiDto(
            isNext = false,
            x = -1, y = -1,
            title = "complete",
            kioskId = kioskId
        ))
        utgService.saveRel(context.lastNode!!.id, completeEntity.id, NodeRelation.PATH_TO)

        val endTime = System.nanoTime()
        log.info("결제 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }
}