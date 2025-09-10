package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.AllUiComponentDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.service.auto.AutoTaskExecutor
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.PageChecker
import com.orderagentservice.order.service.utg.UiDetectorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MenuEditor @Autowired constructor(
    private val autoTaskExecutor: AutoTaskExecutor,
    private val menuNavigator: MenuNavigator,
    private val menuActionExecutor: MenuActionExecutor,
    private val uiDetectorManager: UiDetectorManager,
    private val pageChecker: PageChecker,
    private val graphService: UiGraphService
) {
    fun editCategories(context: GraphContext, categoryList: List<String>, pendingList: List<MenuInfoDto>) {
        //root와 station 가져오기
        val nowNodeId = graphService.findRoot(context.kioskId).id
        context.stationNodeId = graphService.findStation(context.kioskId).id
        val autoContext = AutoOrderContext.toBasicContext(
            kioskId = context.kioskId, taskId = "EDIT", nodeId = nowNodeId,
            place = "매장"
        )

        //메뉴 가기전에 포장/매장 클릭해야할 수도 있음
        autoTaskExecutor.clickPlace(autoContext)

        for (category in categoryList) {
            //카테고리로 이동
            val nodeId = autoTaskExecutor.clickCategory(autoContext, category)

            context.currentCategory = category
            context.lastNodeId = nodeId

            //메뉴 클릭
            val menuList = pendingList.filter { it.category == category }
            menuNavigator.navigateMenus(context, menuList)
        }
    }

    fun editMenus(context: GraphContext, menuList: List<MenuInfoDto>) {
        //root와 station 가져오기
        val nowNodeId = graphService.findRoot(context.kioskId).id
        context.stationNodeId = graphService.findStation(context.kioskId).id
        val autoContext = AutoOrderContext.toBasicContext(
            kioskId = context.kioskId, taskId = "EDIT", nodeId = nowNodeId,
            place = "매장"
        )

        //메뉴 가기전에 포장/매장 클릭해야할 수도 있음
        autoTaskExecutor.clickPlace(autoContext)

        for (menuDto in menuList) {
            //해당 메뉴로 이동. 카테고리 노드 아이디로 업데이트
            val nodeId = autoTaskExecutor.clickMenu(autoContext, menuDto.toAutoOrderMenu()).id
            val categoryNodeId = graphService.findNodeByTitle(context.kioskId, menuDto.category)
            context.lastNodeId = categoryNodeId

            menuNavigator.handleModal(context, menuDto, menuList, nodeId)
        }
    }
}