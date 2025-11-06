package com.orderagentservice.order.service.utg.orchestrator

import com.orderagentservice.logger
import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.result.UtgEditPrepareResult
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.auto.AutoTaskExecutor
import com.orderagentservice.order.service.graph.info.InfoGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.sequencer.MenuActionSequencer
import com.orderagentservice.order.service.utg.UtgActionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UtgUpdateOrchestrator @Autowired constructor(
    private val autoTaskExecutor: AutoTaskExecutor,
    private val utgInitializeOrchestrator: UtgInitializeOrchestrator,
    private val utgActionFactory: UtgActionFactory,
    private val menuActionSequencer: MenuActionSequencer,
    private val uiGraphService: UiGraphService,
    private val infoGraphService: InfoGraphService,
    private val uiDetectorManager: UiDetectorManager,
) {
    private val log = logger()

    fun editCategories(context: UtgContext, modifiedCategoryList: List<String>, pendingList: List<MenuInfoDto>, menuList: List<MenuInfoDto>, isInitPayment: Boolean) {
        val prepareResult = prepareEdit(context)
        val autoContext = prepareResult.autoContext
        val utgActionProfile = prepareResult.actionProfile

        for (category in modifiedCategoryList) {
            //카테고리로 이동
            log.info("수정된 카테고리 노드로 이동합니다 -> ${category}")
            val nodeId = autoTaskExecutor.clickCategory(autoContext, category)

            context.currentCategory = category
            context.lastNodeId = nodeId

            //메뉴 클릭
            val menuList = pendingList.filter { it.category == category }
            log.info("카테고리에 도달 후 메뉴 UTG를 초기화합니다. 메뉴: ${menuList}")
            utgInitializeOrchestrator.navigateMenus(context, menuList, utgActionProfile)
        }

        //완료 된 노드를 파악하고 완료하지 못한 노드를 순회
        navigateRemain(context, menuList, utgActionProfile)

        //수정된 노드의 modified 원상 복구
        modifiedCategoryList.forEach {
            uiGraphService.changeModified(context.kioskId, it)
        }

        if (isInitPayment) {
            utgInitializeOrchestrator.navigatePayment(context, utgActionProfile)
        }
    }

    fun editMenus(context: UtgContext, pendingList: List<MenuInfoDto>, menuList: List<MenuInfoDto>, modifiedMenuList: List<String>, isInitPayment: Boolean) {
        val prepareResult = prepareEdit(context)
        val autoContext = prepareResult.autoContext
        val utgActionProfile = prepareResult.actionProfile

        for (menuDto in pendingList) {
            //해당 메뉴로 이동. 카테고리 노드 아이디로 업데이트
            log.info("수정된 메뉴 노드로 이동합니다 -> ${menuDto.title}")
            var nodeId = autoTaskExecutor.clickMenu(autoContext, menuDto.toAutoOrderMenu()).id
            val categoryNodeId = uiGraphService.findNodeByTitle(context.kioskId, menuDto.category)
            context.lastNodeId = categoryNodeId

            //옵션 처리
            log.info("메뉴에 도달 후 옵션 UTG를 초기화합니다. 옵션: ${menuDto.options}")
            val uiList = uiDetectorManager.getUiComponents(context).uiElements

            // 옵션이 있는 경우 처리
            if (menuDto.options.isNotEmpty()) {
                utgActionProfile.optionSelectStrategy.execute(context, menuDto, nodeId, uiList)
            }

            // 처리 후 뒤로가기 및 관계 저장
            nodeId = utgActionProfile.backSelectStrategy.execute(context, nodeId, uiList, menuDto.options.isNotEmpty())
            uiGraphService.saveRel(nodeId, context.lastNodeId!!, NodeRelationType.BACK_TO)
        }

        //완료 된 노드를 파악하고 완료하지 못한 노드를 순회
        navigateRemain(context, menuList, utgActionProfile)

        //수정된 노드의 modified 원상 복구
        modifiedMenuList.forEach {
            uiGraphService.changeModified(context.kioskId, it)
        }

        if (isInitPayment) {
            utgInitializeOrchestrator.navigatePayment(context, utgActionProfile)
        }
    }

    fun editPayment(context: UtgContext, nowUi: String, randomMenu: MenuInfoDto) {
        val prepareResult = prepareEdit(context)
        val autoContext = prepareResult.autoContext
        val utgActionProfile = prepareResult.actionProfile

        //고친 곳까지 이동
        val actionList = uiGraphService.findPath(context.kioskId, autoContext.nodeId, nowUi)
        //결제를 위해 아무 메뉴나 클릭
        autoTaskExecutor.clickMenu(autoContext, randomMenu.toAutoOrderMenu())
        val last = actionList.last()
        for (action in actionList) {
            autoTaskExecutor.clickPayment(autoContext, action)
        }
        context.lastNodeId = last.id
        log.info("수정된 결제 노드로 이동합니다 -> ${last.title}")

        //complete까지 이동
        utgInitializeOrchestrator.navigatePayment(context, utgActionProfile)
    }

    private fun navigateRemain(context: UtgContext, menuList: List<MenuInfoDto>, utgActionProfile: UtgActionProfile) {
        val completeMenuList = uiGraphService.findAll(context.kioskId)
            .filter { it.type == NodeType.MENU }
            .map { it.title }
        val remainList = menuList.filter { it.title !in completeMenuList }

        log.info("남은 노드를 초기화합니다. 남은 노드: ${remainList}")
        if (remainList.isNotEmpty()) {
            utgInitializeOrchestrator.navigateMenus(context, menuList, utgActionProfile)
        }
    }

    private fun prepareEdit(context: UtgContext): UtgEditPrepareResult {
        //root와 station 가져오기
        val nowNodeId = uiGraphService.findRoot(context.kioskId).id
        context.stationNodeId = uiGraphService.findStation(context.kioskId).id
        val autoContext = AutoOrderContext.toBasicContext(
            kioskId = context.kioskId, taskId = "EDIT", nodeId = nowNodeId,
            place = "매장"
        )

        //메뉴 가기전에 포장/매장 클릭해야할 수도 있음
        autoTaskExecutor.clickPlace(autoContext)

        val utgStrategyRequest = infoGraphService.findLinkedInfo(context.kioskId)
        val utgActionProfile = utgActionFactory.createProfile(utgStrategyRequest)

        return UtgEditPrepareResult(
            autoContext = autoContext,
            actionProfile = utgActionProfile,
        )
    }
}