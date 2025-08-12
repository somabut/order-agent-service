package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.WordSimilarityService
import com.orderagentservice.order.service.graph.GraphService
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MenuNavigator @Autowired constructor(
    private val menuActionExecutor: MenuActionExecutor,
    private val uiExtractorManager: UiExtractorManager,
    private val graphService: GraphService,
    private val wordSimilarityService: WordSimilarityService,
    private val notificationService: NotificationService
) {
    private val log = logger()

    fun navigateMenus(context: GraphContext, menuList: List<MenuInfoDto>) {
        var uiList = uiExtractorManager.getUiComponents(context.kioskId)
        for (menuDto in menuList) {
            if (menuDto.category != context.currentCategory) {
                //카테고리가 다르다면 해당 카테고리로 이동
                menuActionExecutor.selectCategory(context, menuDto, uiList)
                uiList = uiExtractorManager.getUiComponents(context.kioskId)
            }

            log.info("진행 중인 메뉴: ${menuDto.title}, 카테고리: ${menuDto.category}")
            val menuNodeId = menuActionExecutor.selectMenu(context, menuDto, uiList)

            //모달 처리
            handleModal(
                context = context,
                menuDto = menuDto,
                menuNodeId = menuNodeId,
                menuPageList = uiList
            )
        }

        //마지막 노드를 station으로 변경
        context.lastNodeId = context.stationNodeId
    }

    private fun handleModal(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        menuPageList: List<UiComponentDto>
    ) {
        //현재 메뉴를 일단 클릭한 상황
        var nodeId = menuNodeId
        var uiList = uiExtractorManager.getUiComponents(context.kioskId)

        if (menuDto.options.isEmpty()) {
            //옵션이 없는 경우
            if (checkMenuPage(menuPageList, uiList) == false) {
                nodeId = menuActionExecutor.selectModal(
                    context = context,
                    menuDto = menuDto,
                    menuNodeId = menuNodeId,
                    uiList = uiList
                )
            }
            graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelation.BACK_TO)
        } else {
            //옵션이 있는 경우
            if (checkOptionPage(menuDto.options, uiList) == false) {
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
            uiList = uiExtractorManager.getUiComponents(context.kioskId)
            while (checkMenuPage(menuPageList, uiList) == false) {
                nodeId = menuActionExecutor.selectBack(context, nodeId, uiList)
                uiList = uiExtractorManager.getUiComponents(context.kioskId)
            }
            graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelation.BACK_TO)
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

    private fun checkMenuPage(menuPageList: List<UiComponentDto>, uiList: List<UiComponentDto>): Boolean {
        val sourceList = menuPageList.map { it.title }

        val result = wordSimilarityService.determinePage(sourceList, uiList)
        return result
    }

    private fun checkOptionPage(optionList: List<String>, uiList: List<UiComponentDto>): Boolean {
        val result = wordSimilarityService.determinePage(optionList, uiList)
        return result
    }
}