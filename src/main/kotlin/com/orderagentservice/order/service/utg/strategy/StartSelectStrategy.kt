package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.InfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.model.type.StrategyType
import com.orderagentservice.order.service.graph.info.InfoGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

abstract class StartSelectStrategy {
    protected abstract val logService: LogService
    protected abstract val uiGraphService: UiGraphService
    protected abstract val infoGraphService: InfoGraphService

    abstract fun execute(context: UtgContext, utgStrategyRequest: UtgStrategyRequest)

    protected fun setupNode(context: UtgContext, utgStrategyRequest: UtgStrategyRequest) {
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
        context.lastNodeId = uiGraphService.saveNode(rootUiDto).id

        // UTG정보 노드 저장 및 관계
        val infoDto = InfoDto(
            startStrategy = utgStrategyRequest.startStrategy,
            optionStrategy = utgStrategyRequest.optionStrategy,
            backStrategy = utgStrategyRequest.backStrategy,
            paymentStrategy = utgStrategyRequest.paymentStrategy,
        )
        val infoNodeId = infoGraphService.saveNode(infoDto).id
        infoGraphService.saveRel(context.kioskId, infoNodeId)


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
        context.stationNodeId = uiGraphService.saveNode(stationNode).id

        uiGraphService.saveRel(context.lastNodeId!!, context.stationNodeId!!, NodeRelationType.PATH_TO)
    }
}

@Component(StrategyType.IN_START_PLACE)
class PlaceStartSelectStrategy @Autowired constructor(
    override val logService: LogService,
    override val uiGraphService: UiGraphService,
    override val infoGraphService: InfoGraphService,
    private val placeUtgService: PlaceUtgService,
    private val uiDetectorManager: UiDetectorManager
) : StartSelectStrategy() {
    override fun execute(context: UtgContext, utgStrategyRequest: UtgStrategyRequest) {
        // root, station노드 초기화
        setupNode(context, utgStrategyRequest)

        //포장/매장 찾기
        val uiList = uiDetectorManager.getUiComponents(context, true).ocrElements
        placeUtgService.initializeGraph(context, uiList)
    }
}

@Component(StrategyType.EX_START_PLACE)
class DefaultStartSelectStrategy @Autowired constructor(
    override val logService: LogService,
    override val uiGraphService: UiGraphService,
    override val infoGraphService: InfoGraphService,
) : StartSelectStrategy() {
    override fun execute(context: UtgContext, utgStrategyRequest: UtgStrategyRequest) {
        // root, station노드 초기화
        setupNode(context, utgStrategyRequest)
    }
}