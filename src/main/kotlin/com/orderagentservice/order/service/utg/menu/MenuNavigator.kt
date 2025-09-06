package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.exception.UtgInfiniteLoopException
import com.orderagentservice.order.model.type.ExtractType
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.log.UtgNowMenuLog
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.PageChecker
import com.orderagentservice.order.service.utg.ScreenNodeGenerator
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.WordSimilarityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuNavigator @Autowired constructor(
    private val menuActionExecutor: MenuActionExecutor,
    private val uiDetectorManager: UiDetectorManager,
    private val graphService: UiGraphService,
    private val notificationService: NotificationService,
    private val screenNodeGenerator: ScreenNodeGenerator,
    private val pageChecker: PageChecker,
    private val logService: LogService
) {
    private val MAX_LOOP = 5

    fun navigateMenus(context: GraphContext, menuList: List<MenuInfoDto>) {
        var uiList = uiDetectorManager.getUiComponents(context).uiElements
        for (menuDto in menuList) {
            if (menuDto.category != context.currentCategory) {
                //카테고리가 다르다면 해당 카테고리로 이동
                val creationResult = menuActionExecutor.selectCategory(context, menuDto, uiList)
                uiList = uiDetectorManager.getUiComponents(context).uiElements

                //match 노드와 관계, screen 노드와 관계 연결
                screenNodeGenerator.linkNode(
                    kioskId = context.kioskId,
                    nodeId = creationResult.nodeId, screenNodeId = context.screenNodeId,
                    uiComponentParams = creationResult.uiComponentParams,
                )
            }

            logService.printLog(
                UtgNowMenuLog(
                    kioskId = context.kioskId,
                    menu = menuDto.title,
                    category = menuDto.category
                )
            )
            val menuNodeId = menuActionExecutor.selectMenu(context, menuDto, uiList)

            //모달 처리
            handleModal(
                context = context,
                menuDto = menuDto,
                menuNodeId = menuNodeId,
                menuList = menuList,
            )
        }

        //마지막 노드를 station으로 변경
        context.lastNodeId = context.stationNodeId
    }

    fun handleModal(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuList: List<MenuInfoDto>,
        menuNodeId: String,
    ) {
        //현재 메뉴를 일단 클릭한 상황
        var nodeId = menuNodeId
        var uiList = uiDetectorManager.getUiComponents(context).uiElements

        if (menuDto.options.isEmpty()) {
            //옵션이 없는 경우

            //모달처리
            if (pageChecker.checkMenuPage(menuDto, menuList, uiList) == false) {
                logService.printLog(
                    UtgProcessLog(
                        kioskId = context.kioskId,
                        message = "모달이 감지되어 모달을 처리합니다."
                    )
                )
                nodeId = menuActionExecutor.selectModal(
                    context = context,
                    menuDto = menuDto,
                    menuNodeId = menuNodeId,
                    uiList = uiList
                )
                graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelationType.BACK_TO)
            }
        } else {
            //옵션이 있는 경우

            //모달처리
            if (pageChecker.checkOptionPage(menuDto.options, uiList) == false) {
                logService.printLog(
                    UtgProcessLog(
                        kioskId = context.kioskId,
                        message = "모달이 감지되어 모달을 처리합니다."
                    )
                )
                nodeId = menuActionExecutor.selectModal(
                    context = context,
                    menuDto = menuDto,
                    menuNodeId = menuNodeId,
                    uiList = uiList
                )
            }

            //옵션으로 왔으므로 옵션선택
            menuActionExecutor.selectOption(context, menuDto, nodeId)

            //옵션을 선택하고 원래 페이지도 이동
            var count = 0
            uiList = uiDetectorManager.getUiComponents(context).uiElements
            while (pageChecker.checkMenuPage(menuDto, menuList, uiList) == false) {
                if (count >= MAX_LOOP) {
                    throw UtgInfiniteLoopException()
                }

                nodeId = menuActionExecutor.selectBack(context, nodeId, uiList)
                uiList = uiDetectorManager.getUiComponents(context).uiElements
                count++
            }
            graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelationType.BACK_TO)
        }
    }

    fun moveCategory(context: GraphContext, categoryMenuList: List<MenuInfoDto>) {
        val category = categoryMenuList[0].category

        //변경이 일어난 카테고리까지 이동
        val actionList = graphService.findPath(context.kioskId, context.lastNodeId!!, category)
        for (act in actionList) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(act.x, act.y, act.title))
        }

        context.lastNodeId = actionList.last().id
        context.currentCategory = category

        //해당 카테고리의 메뉴 제거
        val categoryId = actionList.last().id
        graphService.deleteMenusByCategory(context.kioskId, categoryId)
    }
}