package com.orderagentservice.order.service

import com.orderagentservice.agent.PlaceAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PlaceGraphInitializeService @Autowired constructor(
    private val placeAgent: PlaceAgent,
    private val notificationService: NotificationService,
    private val utgService: UtgService
) {
    private val log = logger()

    fun initializeGraph(kioskId: String, lastNode: UiEntity, llmUiList: List<LlmUiComponentDto>): List<AgentActionDto> {
        val history = mutableListOf<AgentActionDto>()
        val action = placeAgent.determineAction(llmUiList)
        if (action.size <= 1) {
            log.info("포장/매장 UI를 발견하지 못했습니다.")
            return history
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
            utgService.saveRel(lastNode.id, entity.id, NodeRelation.HAS_TO)
            history.add(act)
            log.info("포장/매장 노드를 생성합니다. go_next: ${act.goNext}, score: ${act.score}, coordinate: ${x}, title: ${y}")
        }

        //키오스크에 따라 버튼을 클릭해야 넘어가는 경우가 있으므로 클릭
        notificationService.sendActionCommand(kioskId, CoordinateDto(action[0].coordinate[0], action[0].coordinate[1], action[0].title))

        return history
    }
}