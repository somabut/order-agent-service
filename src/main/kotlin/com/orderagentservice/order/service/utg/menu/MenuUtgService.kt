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

    fun updateCategory(context: GraphContext, menuList: List<MenuInfoDto>) {
        //수정된 카테고리까지 가서 메뉴 노드 그리기
        val uiDtoList = graphService.findModified(context.kioskId)

        val modifiedCategoryList = uiDtoList
            .filter { it.type == NodeType.CATEGORY }
            .map { it.title }
        val pendingList = menuList.filter { it.category in modifiedCategoryList }

        menuEditor.editCategories(context, modifiedCategoryList, pendingList)

        //완료 된 노드를 파악하고 완료하지 못한 노드를 순회
        val completeMenuList = graphService.findAll(context.kioskId)
            .filter { it.type == NodeType.MENU }
            .map { it.title }
        val remainList = menuList.filter { it.title !in completeMenuList }

        menuNavigator.navigateMenus(context, remainList)
    }

    fun updateMenu(context: GraphContext, menuList: List<MenuInfoDto>) {
        //수정된 메뉴까지 가서 옵션 노드 그리기
        val uiDtoList = graphService.findModified(context.kioskId)

        val modifiedMenuList = uiDtoList
            .filter { it.type == NodeType.MENU }
            .map { it.title }
        val pendingList = menuList.filter { it.title in modifiedMenuList }

        menuEditor.editMenus(context, pendingList, menuList)

        //이후 아직 탐색 못한 메뉴 탐색
        val completeMenuList = graphService.findAll(context.kioskId)
            .filter { it.type == NodeType.MENU }
            .map { it.title }
        val remainList = menuList.filter { it.title !in completeMenuList }

        menuNavigator.navigateMenus(context, remainList)
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