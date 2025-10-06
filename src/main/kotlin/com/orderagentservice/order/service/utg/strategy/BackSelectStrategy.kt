package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.type.StrategyType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.ComparatorManager
import com.orderagentservice.order.service.utg.ScreenNodeIntegrator
import com.orderagentservice.order.service.utg.menu.UiNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

abstract class BackSelectStrategy {
    protected abstract val notificationService: NotificationService
    protected abstract val uiNodeIntegrator: UiNodeIntegrator
    protected abstract val screenNodeIntegrator: ScreenNodeIntegrator
    protected abstract val comparatorManager: ComparatorManager
    protected abstract val backAgent: BackAgent
    protected abstract val logService: LogService

    private val log = logger()

    abstract fun execute(context: UtgContext, menuNodeId: String, uiList: List<UiComponentDto>, hasOption: Boolean): String

    protected fun navigateBack(
        context: UtgContext,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): String {
        val kioskId = context.kioskId

        //캐시된 것이 있는 지 확인
        val backUi = cacheBackUi(context, uiList)

        //노드 생성
        val creationResult = uiNodeIntegrator.integrateBackNode(backUi, menuNodeId, context)

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeIntegrator.linkNode(
            kioskId = context.kioskId,
            nodeId = creationResult.nodeId, screenNodeId = context.screenNodeId,
            uiComponentParams = creationResult.uiComponentParams,
        )

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        logService.printLog(
            UtgProcessLog(
                kioskId = kioskId,
                message = "돌아가는 좌표를 클릭중입니다. 좌표: ${backUi.coordinate}"
            )
        )
        val (x, y) = backUi.coordinate
        notificationService.sendActionCommand(kioskId, CoordinateDto(x, y, backUi.title))

        return creationResult.nodeId
    }

    private fun cacheBackUi(context: UtgContext, uiList: List<UiComponentDto>): AgentBackDto {
        var backUi: AgentBackDto
        if (context.menuBackUi == null) {
            //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
            val agentUiList = uiList
            val backAction = backAgent.determineAction(agentUiList)
            context.menuBackUi = backAction.title

            backUi = AgentBackDto(
                score = backAction.score,
                coordinate = backAction.coordinate, bbox = backAction.bbox,
                title = backAction.title
            )
        } else {
            //UI가 캐싱된 경우
            log.info("back UI 캐시 히트")
            val matchDto = comparatorManager.wordCompare(context.menuBackUi!!, uiList)
            backUi = AgentBackDto(
                score = 1.0F,
                coordinate = listOf(matchDto.x, matchDto.y), bbox = listOf(matchDto.minX, matchDto.minY, matchDto.maxX, matchDto.maxY),
                title = matchDto.title
            )
        }

        return backUi
    }
}

@Component(StrategyType.IN_BACK)
class IncludeBackSelectStrategy @Autowired constructor(
    override val notificationService: NotificationService,
    override val uiNodeIntegrator: UiNodeIntegrator,
    override val screenNodeIntegrator: ScreenNodeIntegrator,
    override val comparatorManager: ComparatorManager,
    override val backAgent: BackAgent,
    override val logService: LogService
) : BackSelectStrategy() {
    override fun execute(
        context: UtgContext, menuNodeId: String,
        uiList: List<UiComponentDto>, hasOption: Boolean
    ): String {
        return navigateBack(
            context = context,
            menuNodeId = menuNodeId,
            uiList = uiList
        )
    }
}

@Component(StrategyType.OP_BACK)
class OptionalBackSelectStrategy @Autowired constructor(
    override val notificationService: NotificationService,
    override val uiNodeIntegrator: UiNodeIntegrator,
    override val screenNodeIntegrator: ScreenNodeIntegrator,
    override val comparatorManager: ComparatorManager,
    override val backAgent: BackAgent,
    override val logService: LogService
) : BackSelectStrategy() {
    override fun execute(
        context: UtgContext, menuNodeId: String,
        uiList: List<UiComponentDto>, hasOption: Boolean
    ): String {
        if (hasOption) {
            return navigateBack(
                context = context,
                menuNodeId = menuNodeId,
                uiList = uiList
            )
        }
        return ""
    }
}

@Component(StrategyType.EX_BACK)
class NoneBackSelectStrategy @Autowired constructor(
    override val notificationService: NotificationService,
    override val uiNodeIntegrator: UiNodeIntegrator,
    override val screenNodeIntegrator: ScreenNodeIntegrator,
    override val comparatorManager: ComparatorManager,
    override val backAgent: BackAgent,
    override val logService: LogService
): BackSelectStrategy() {
    override fun execute(context: UtgContext, menuNodeId: String, uiList: List<UiComponentDto>, hasOption: Boolean): String {
        return ""
    }
}
