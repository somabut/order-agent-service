package com.orderagentservice.order.service.utg.place

import com.orderagentservice.agent.PlaceAgent
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.type.SaveNodeType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.GraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.log

@Service
class PlaceUtgService @Autowired constructor(
    private val placeAgent: PlaceAgent,
    private val notificationService: NotificationService,
    private val uiDetectorManager: UiDetectorManager,
    private val graphService: GraphService,
    private val logService: LogService
) {
    @Transactional
    fun initializeGraph(context: GraphContext) {
        val kioskId = context.kioskId
        val uiList = uiDetectorManager.getUiComponents(context)
        val action = placeAgent.determineAction(uiList)

        if (action.size <= 1) {
            logService.printLog(
                UtgProcessLog(
                    kioskId = context.kioskId,
                    message = "포장/매장 UI를 발견하지 못했습니다."
                )
            )
            return
        }

        //포장 / 매장 UI 탐색
        for (act in action) {
            val x = act.coordinate[0]
            val y = act.coordinate[1]
            val entity = graphService.saveNode(
                UiDto(
                    isNext = false,
                    x = x, y = y,
                    title = act.title,
                    kioskId = kioskId
                )
            )
            graphService.saveRel(context.lastNodeId!!, entity.id, NodeRelationType.HAS_TO)
            context.history.add(act)
            logService.printLog(
                NodeSaveLog(
                    kioskId = context.kioskId,
                    nodeType = SaveNodeType.PLACE,
                    x = x, y = y,
                    title = act.title, imageName = context.imageName
                )
            )
        }
        context.isPlaceDetermined = true

        //키오스크에 따라 버튼을 클릭해야 넘어가는 경우가 있으므로 클릭
        notificationService.sendActionCommand(kioskId, CoordinateDto(action[0].coordinate[0], action[0].coordinate[1], action[0].title))
    }
}