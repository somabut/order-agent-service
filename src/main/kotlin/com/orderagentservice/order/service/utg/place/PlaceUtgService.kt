package com.orderagentservice.order.service.utg.place

import com.orderagentservice.agent.PlaceAgent
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.ScreenNodeGenerator
import com.orderagentservice.order.service.utg.UiDetectorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceUtgService @Autowired constructor(
    private val placeAgent: PlaceAgent,
    private val notificationService: NotificationService,
    private val uiDetectorManager: UiDetectorManager,
    private val graphService: UiGraphService,
    private val screenNodeGenerator: ScreenNodeGenerator,
    private val logService: LogService
) {
    @Transactional
    fun initializeGraph(context: GraphContext) {
        val kioskId = context.kioskId
        val uiList = uiDetectorManager.getUiComponents(context).uiElements
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
            val (x, y) = act.coordinate
            val (minX, minY, maxX, maxY) = act.bbox

            val node = graphService.saveNode(
                UiDto(
                    isNext = false,
                    x = x, y = y,
                    title = act.title,
                    kioskId = kioskId
                )
            )
            graphService.saveRel(context.lastNodeId!!, node.id, NodeRelationType.HAS_TO)

            //match 노드와 관계, screen 노드와 관계 연결
            screenNodeGenerator.linkNode(
                kioskId = context.kioskId,
                nodeId = node.id, screenNodeId = context.screenNodeId,
                UiComponentParams(
                    minX = minX, minY = minY,
                    maxX = maxX, maxY = maxY,
                    title = act.title
                )
            )

            context.history.add(act)
            logService.printLog(
                NodeSaveLog(
                    kioskId = context.kioskId,
                    nodeType = NodeType.PLACE,
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