package com.orderagentservice.order.service

import com.orderagentservice.agent.PlaceAgent
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphInitializeContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceGraphInitializeService @Autowired constructor(
    private val placeAgent: PlaceAgent,
    private val notificationService: NotificationService,
    private val uiExtractorManager: UiExtractorManager,
    private val utgService: UtgService
) {
    private val log = logger()

    @Transactional
    fun initializeGraph(context: GraphInitializeContext) {
        val kioskId = context.kioskId
        val uiList = uiExtractorManager.getUiComponents(context.kioskId)
        val action = placeAgent.determineAction(uiList)
        if (action.size <= 1) {
            log.info("포장/매장 UI를 발견하지 못했습니다.")
            return
        }

        //포장 / 매장 UI 탐색
        for (act in action) {
            val x = act.coordinate[0]
            val y = act.coordinate[1]
            val entity = utgService.saveNode(UiDto(
                    isNext = false,
                    x = x, y = y,
                    title = act.title,
                    kioskId = kioskId
                ))
            utgService.saveRel(context.lastNode!!.id, entity.id, NodeRelation.HAS_TO)
            context.history.add(act)
            log.info("포장/매장 노드를 생성합니다. go_next: ${act.goNext}, score: ${act.score}, coordinate: $x $y, title: ${act.title}")
        }
        context.determinePlace = true

        //키오스크에 따라 버튼을 클릭해야 넘어가는 경우가 있으므로 클릭
        notificationService.sendActionCommand(kioskId, CoordinateDto(action[0].coordinate[0], action[0].coordinate[1], action[0].title))
    }
}