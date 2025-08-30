package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.exception.UtgInfiniteLoopException
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.UtgNowMenuLog
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.GraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.WordSimilarityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class MenuNavigator @Autowired constructor(
    private val menuActionExecutor: MenuActionExecutor,
    private val uiDetectorManager: UiDetectorManager,
    private val graphService: GraphService,
    private val wordSimilarityService: WordSimilarityService,
    private val notificationService: NotificationService,
    private val logService: LogService
) {
    private val MAX_LOOP = 5

    fun navigateMenus(context: GraphContext, menuList: List<MenuInfoDto>) {
        var uiList = uiDetectorManager.getUiComponents(context)
        for (menuDto in menuList) {
            if (menuDto.category != context.currentCategory) {
                //카테고리가 다르다면 해당 카테고리로 이동
                menuActionExecutor.selectCategory(context, menuDto, uiList)
                uiList = uiDetectorManager.getUiComponents(context)
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

    private fun handleModal(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuList: List<MenuInfoDto>,
        menuNodeId: String,
    ) {
        //현재 메뉴를 일단 클릭한 상황
        var nodeId = menuNodeId
        var uiList = uiDetectorManager.getUiComponents(context)

        if (menuDto.options.isEmpty()) {
            //옵션이 없는 경우

            //모달처리
            if (checkMenuPage(menuDto, menuList, uiList) == false) {
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
            if (checkOptionPage(menuDto.options, uiList) == false) {
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
            uiList = uiDetectorManager.getUiComponents(context)
            while (checkMenuPage(menuDto, menuList, uiList) == false) {
                if (count >= MAX_LOOP) {
                    throw UtgInfiniteLoopException()
                }

                nodeId = menuActionExecutor.selectBack(context, nodeId, uiList)
                uiList = uiDetectorManager.getUiComponents(context)
                count++
                println("찾는메뉴: ${menuDto.title}, 리스트: ${menuList}")
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

    private fun checkMenuPage(menuDto: MenuInfoDto, menuList: List<MenuInfoDto>, uiList: List<UiComponentDto>): Boolean {
        val sourceList = getMenusByCategory(menuDto, menuList)

        val result = wordSimilarityService.determinePage(sourceList, uiList)
        return result
    }

    private fun checkOptionPage(optionList: List<String>, uiList: List<UiComponentDto>): Boolean {
        val result = wordSimilarityService.determinePage(optionList, uiList)
        return result
    }

    private fun getMenusByCategory(menuDto: MenuInfoDto, menuList: List<MenuInfoDto>): List<String> {
        val filteredList = menuList
            .filter { it.category == menuDto.category }
            .map { it.title }

        return filteredList
    }
}