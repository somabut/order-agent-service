package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.model.type.StrategyType
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

abstract class StartSelectStrategy {
    protected abstract val logService: LogService
    protected abstract val graphService: UiGraphService

    abstract fun execute(context: UtgContext)

    protected fun setupNode(context: UtgContext) {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId, nodeType = NodeType.ROOT,
                x = -1, y = -1,
                title = SpecialNodeType.ROOT.title, imageName = context.imageName
            )
        )
        val kioskId = context.kioskId
        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = SpecialNodeType.ROOT.title,
            type = NodeType.ROOT
        )
        context.lastNodeId = graphService.saveNode(rootUiDto).id

        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId, nodeType = NodeType.STATION,
                x = -1, y = -1,
                title = SpecialNodeType.STATION.title, imageName = context.imageName
            )
        )
        val stationNode = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = SpecialNodeType.STATION.title,
            type = NodeType.STATION
        )
        context.stationNodeId = graphService.saveNode(stationNode).id

        graphService.saveRel(context.lastNodeId!!, context.stationNodeId!!, NodeRelationType.PATH_TO)
    }
}

@Component(StrategyType.IN_START_PLACE)
class PlaceStartSelectStrategy @Autowired constructor(
    override val logService: LogService,
    override val graphService: UiGraphService,
    private val placeUtgService: PlaceUtgService,
    private val uiDetectorManager: UiDetectorManager
) : StartSelectStrategy() {
    override fun execute(context: UtgContext) {
        // root, station노드 초기화
        setupNode(context)

        //포장/매장 찾기
        val uiList = uiDetectorManager.getUiComponents(context, true).ocrElements
        placeUtgService.initializeGraph(context, uiList)
    }
}

@Component(StrategyType.EX_START_PLACE)
class DefaultStartSelectStrategy @Autowired constructor(
    override val logService: LogService,
    override val graphService: UiGraphService,
) : StartSelectStrategy() {
    private val log = logger();

    override fun execute(context: UtgContext) {
        // root, station노드 초기화
        setupNode(context)
    }
}