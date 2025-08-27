package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.log.UtgEndLog
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.log.UtgStartLog
import com.orderagentservice.order.model.type.SaveNodeType
import com.orderagentservice.order.model.type.UtgType
import com.orderagentservice.order.service.graph.GraphService
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenuUtgService @Autowired constructor(
    private val menuNavigator: MenuNavigator,
    private val placeUtgService: PlaceUtgService,
    private val graphService: GraphService,
    private val logService: LogService,
    private val usageTracker: UsageTracker
)  {

    @Transactional
    fun initializeGraph(context: GraphContext, menuList: List<MenuInfoDto>) {
        logService.printLog(
            UtgStartLog(
                kioskId = context.kioskId,
                utgType = UtgType.MENU
            )
        )
        val startTime = System.nanoTime()

        // root, station노드 초기화
        setupNode(context)

        //포장/매장 찾기
        placeUtgService.initializeGraph(context)

        //루프를 돌며 메뉴들을 모두 탐색
        menuNavigator.navigateMenus(context, menuList)

        //포장/매장 찾기
        if (context.isPlaceDetermined == false) {
            placeUtgService.initializeGraph(context)
        }

        val endTime = System.nanoTime()
        logService.printLog(
            UtgEndLog(
                kioskId = context.kioskId,
                utgType = UtgType.MENU,
                processingTime = (endTime - startTime) / 1000000,
                totalTokenUsage = usageTracker.totalUsage
            )
        )
    }

    @Transactional
    fun updateGraph(context: GraphContext, categoryMenuList: List<MenuInfoDto>) {
        logService.printLog(
            UtgStartLog(
                kioskId = context.kioskId,
                utgType = UtgType.UPDATE
            )
        )
        val startTime = System.nanoTime()

        val nowNodeId = graphService.findRoot(context.kioskId).id
        context.lastNodeId = nowNodeId

        //카테고리로 이동
        menuNavigator.moveCategory(context, categoryMenuList)

        //해당 페이지에서 메뉴 탐색
        menuNavigator.navigateMenus(context, categoryMenuList)

        val endTime = System.nanoTime()
        logService.printLog(
            UtgEndLog(
                kioskId = context.kioskId,
                utgType = UtgType.UPDATE,
                processingTime = (endTime - startTime) / 1000000,
                totalTokenUsage = usageTracker.totalUsage
            )
        )
    }

    private fun setupNode(context: GraphContext) {
        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId, nodeType = SaveNodeType.ROOT,
                x = -1, y = -1,
                title = SpecialNodeType.ROOT.title, imageName = context.imageName
            )
        )
        val kioskId = context.kioskId
        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = SpecialNodeType.ROOT.title
        )
        context.lastNodeId = graphService.saveNode(rootUiDto).id

        logService.printLog(
            NodeSaveLog(
                kioskId = context.kioskId, nodeType = SaveNodeType.STATION,
                x = -1, y = -1,
                title = SpecialNodeType.STATION.title, imageName = context.imageName
            )
        )
        val stationNode = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = SpecialNodeType.STATION.title
        )
        context.stationNodeId = graphService.saveNode(stationNode).id

        graphService.saveRel(context.lastNodeId!!, context.stationNodeId!!, NodeRelationType.PATH_TO)
    }
}