package com.orderagentservice.order.service.utg.sequencer

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.result.CategorySequenceResult
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.UiDetectorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CategoryActionSequencer @Autowired constructor(
    private val uiDetectorManager: UiDetectorManager,
    private val screenNodeIntegrator: ScreenNodeIntegrator,
) {
    private val log = logger()

    fun run(
        context: UtgContext,
        menuDto: MenuInfoDto,
        actionProfile: UtgActionProfile,
        uiList: List<UiComponentDto>,
        originCategoryScreenId: String
    ): CategorySequenceResult {
        if (menuDto.category != context.currentCategory) {
            log.info("카테고리 이동: ${menuDto.category}")
            var currentUiList = uiDetectorManager.getUiComponents(context).uiElements

            val creationResult = actionProfile.categorySelectStrategy.execute(context, menuDto, currentUiList)

            screenNodeIntegrator.linkNode(
                kioskId = context.kioskId,
                nodeId = creationResult.nodeId,
                screenNodeId = context.screenNodeId,
                uiComponentParams = creationResult.uiComponentParams,
            )

            return CategorySequenceResult(
                uiList = uiDetectorManager.getUiComponents(context).uiElements,
                categoryScreenId = context.screenNodeId
            )
        }

        // 카테고리 이동이 필요 없는 경우 기존 상태를 그대로 반환
        return CategorySequenceResult(
            uiList = uiList,
            categoryScreenId = originCategoryScreenId
        )
    }
}