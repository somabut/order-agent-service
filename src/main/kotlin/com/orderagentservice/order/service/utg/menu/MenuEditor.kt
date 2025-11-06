package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.logger
import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.auto.AutoTaskExecutor
import com.orderagentservice.order.service.graph.info.InfoGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.sequencer.MenuActionSequencer
import com.orderagentservice.order.service.utg.UtgActionFactory
import com.orderagentservice.order.service.utg.orchestrator.UtgInitializeOrchestrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MenuEditor @Autowired constructor(
    private val autoTaskExecutor: AutoTaskExecutor,
    private val menuNavigator: MenuNavigator,
    private val uiGraphService: UiGraphService,
    private val notificationService: NotificationService,
) {
    val log = logger()

    fun editCategories(context: UtgContext, modifiedCategoryList: List<String>, pendingList: List<MenuInfoDto>) {
        //root와 station 가져오기
        val nowNodeId = uiGraphService.findRoot(context.kioskId).id
        context.stationNodeId = uiGraphService.findStation(context.kioskId).id
        val autoContext = AutoOrderContext.toBasicContext(
            kioskId = context.kioskId, taskId = "EDIT", nodeId = nowNodeId,
            place = "매장"
        )

        //메뉴 가기전에 포장/매장 클릭해야할 수도 있음
        autoTaskExecutor.clickPlace(autoContext)

        for (category in modifiedCategoryList) {
            //카테고리로 이동
            log.info("수정된 카테고리 노드로 이동합니다 -> ${category}")
            val nodeId = autoTaskExecutor.clickCategory(autoContext, category)

            context.currentCategory = category
            context.lastNodeId = nodeId

            //메뉴 클릭
            val menuList = pendingList.filter { it.category == category }
            log.info("카테고리에 도달 후 메뉴 UTG를 초기화합니다. 메뉴: ${menuList}")
            menuNavigator.navigateMenus(context, menuList)
        }
    }

    fun editMenus(context: UtgContext, modifiedMenuList: List<MenuInfoDto>) {
        //root와 station 가져오기
        val nowNodeId = uiGraphService.findRoot(context.kioskId).id
        context.stationNodeId = uiGraphService.findStation(context.kioskId).id
        val autoContext = AutoOrderContext.toBasicContext(
            kioskId = context.kioskId, taskId = "EDIT", nodeId = nowNodeId,
            place = "매장"
        )

        //메뉴 가기전에 포장/매장 클릭해야할 수도 있음
        autoTaskExecutor.clickPlace(autoContext)

        for (menuDto in modifiedMenuList) {
            //해당 메뉴로 이동. 카테고리 노드 아이디로 업데이트
            log.info("수정된 메뉴 노드로 이동합니다 -> ${menuDto.title}")
            val sourceCapture = notificationService.sendCaptureCommand(context.kioskId)
            val nodeId = autoTaskExecutor.clickMenu(autoContext, menuDto.toAutoOrderMenu()).id
            val targetCapture = notificationService.sendCaptureCommand(context.kioskId)
            val categoryNodeId = uiGraphService.findNodeByTitle(context.kioskId, menuDto.category)
            context.lastNodeId = categoryNodeId

            //옵션 처리
            log.info("메뉴에 도달 후 옵션 UTG를 초기화합니다. 옵션: ${menuDto.options}")
            menuNavigator.handleModal(
                context = context,
                sourceCapture = sourceCapture,
                targetCapture = targetCapture,
                menuDto = menuDto,
                menuNodeId = nodeId
            )
        }
    }
}