package com.orderagentservice.order.service.utg.sequencer

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuActionSequencer @Autowired constructor(
    private val uiDetectorManager: UiDetectorManager,
    private val graphService: UiGraphService,
    private val logService: LogService
){
    fun run(
        context: UtgContext,
        menuDto: MenuInfoDto,
        actionProfile: UtgActionProfile,
        uiList: List<UiComponentDto>,
        categoryScreenId: String,
    ) {
        // 메뉴 선택
        var nodeId = actionProfile.menuSelectStrategy.execute(context, menuDto, uiList, categoryScreenId)
        val updatedUiList = uiDetectorManager.getUiComponents(context).uiElements

        // 옵션이 있는 경우 처리
        if (menuDto.options.isNotEmpty()) {
            actionProfile.optionSelectStrategy.execute(context, menuDto, nodeId, updatedUiList)
        }

        // 처리 후 뒤로가기 및 관계 저장
        nodeId = actionProfile.backSelectStrategy.execute(context, nodeId, updatedUiList, menuDto.options.isNotEmpty())
        graphService.saveRel(nodeId, context.lastNodeId!!, NodeRelationType.BACK_TO)
    }
}