package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.KioskCaptureDto
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.UtgNowMenuLog
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.PageChecker
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.UiDetectorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuNavigator @Autowired constructor(
    private val menuActionExecutor: MenuActionExecutor,
    private val uiDetectorManager: UiDetectorManager,
    private val graphService: UiGraphService,
    private val screenNodeIntegrator: ScreenNodeIntegrator,
    private val pageChecker: PageChecker,
    private val notificationService: NotificationService,
    private val comparatorManager: ComparatorManager,
    private val logService: LogService
) {
    private val log = logger()
    private val MAX_LOOP = 5

    fun navigateMenus(context: UtgContext, menuList: List<MenuInfoDto>) {
        var uiList: List<UiComponentDto> = uiDetectorManager.getUiComponents(context).uiElements
        var categoryScreenId = context.screenNodeId
        for (menuDto in menuList) {
            if (menuDto.category != context.currentCategory) {
                //카테고리가 다르다면 해당 카테고리로 이동
                log.info("카테고리 이동: ${menuDto.category}")
                uiList = uiDetectorManager.getUiComponents(context).uiElements
                val creationResult = menuActionExecutor.selectCategory(context, menuDto, uiList)

                //카테고리와 match 노드와 관계, screen 노드와 관계 연결
                screenNodeIntegrator.linkNode(
                    kioskId = context.kioskId,
                    nodeId = creationResult.nodeId, screenNodeId = context.screenNodeId,
                    uiComponentParams = creationResult.uiComponentParams,
                )

                uiList = uiDetectorManager.getUiComponents(context).uiElements
                categoryScreenId = context.screenNodeId
            }

            logService.printLog(
                UtgNowMenuLog(
                    kioskId = context.kioskId,
                    menu = menuDto.title,
                    category = menuDto.category
                )
            )
            val sourceCapture = notificationService.sendCaptureCommand(context.kioskId)
            val menuNodeId = menuActionExecutor.selectMenu(context, menuDto, uiList, categoryScreenId)
            val targetCapture = notificationService.sendCaptureCommand(context.kioskId)

            //모달 처리
            handleModal(
                context = context,
                sourceCapture = sourceCapture,
                targetCapture = targetCapture,
                menuDto = menuDto,
                menuNodeId = menuNodeId,
            )
        }

        //마지막 노드를 station으로 변경
        context.lastNodeId = context.stationNodeId
    }

    fun handleModal(
        context: UtgContext,
        sourceCapture: KioskCaptureDto,
        targetCapture: KioskCaptureDto,
        menuDto: MenuInfoDto,
        menuNodeId: String,
    ) {
        //현재 메뉴를 일단 클릭한 상황
        var nodeId = menuNodeId
        val uiList = uiDetectorManager.getUiComponents(context).uiElements

        if (menuDto.options.isEmpty()) {
            //옵션이 없는 경우

            //모달처리 (이미지 기반)
            if (comparatorManager.imageCompare(
                    sourceCapture.content, sourceCapture.type,
                    targetCapture.content, targetCapture.type
            ) == false) {
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

            //옵션 처리
            menuActionExecutor.selectOption(context, menuDto, nodeId, uiList)
            nodeId = menuActionExecutor.selectBack(context, nodeId, uiList)

            graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelationType.BACK_TO)
        }
    }
}