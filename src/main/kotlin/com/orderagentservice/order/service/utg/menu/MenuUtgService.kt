package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.log.UtgEndLog
import com.orderagentservice.order.model.log.UtgStartLog
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.model.type.UtgType
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenuUtgService @Autowired constructor(
    private val menuNavigator: MenuNavigator,
    private val menuEditor: MenuEditor,
    private val uiDetectorManager: UiDetectorManager,
    private val placeUtgService: PlaceUtgService,
    private val graphService: UiGraphService,
    private val logService: LogService,
    private val usageTracker: UsageTracker
)  {
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
        val uiList = uiDetectorManager.getUiComponents(context).ocrElements
        placeUtgService.initializeGraph(context, uiList)

        //루프를 돌며 메뉴들을 모두 탐색
        menuNavigator.navigateMenus(context, menuList)

        //포장/매장 찾기
        if (context.isPlaceDetermined == false) {
            val uiList = uiDetectorManager.getUiComponents(context).ocrElements
            placeUtgService.initializeGraph(context, uiList)
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

    fun updateCategory(context: GraphContext, categoryList: List<String>, pendingList: List<MenuInfoDto>) {
        //수정된 카테고리까지 가서 메뉴 노드 그리기
        menuEditor.editCategories(context, categoryList, pendingList)

        //남은 노드는 일반 탐색
        val remainList = pendingList.filter { it.category !in categoryList }
        menuNavigator.navigateMenus(context, remainList)
    }

    fun updateMenu(context: GraphContext, updatedMenus: List<MenuInfoDto>, pendingMenus: List<MenuInfoDto>) {
        //수정된 메뉴까지 가서 옵션 노드 그리기
        menuEditor.editMenus(context, updatedMenus)

        //이후 아직 탐색 못한 메뉴 탐색
        menuNavigator.navigateMenus(context, pendingMenus)
    }

    private fun setupNode(context: GraphContext) {
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