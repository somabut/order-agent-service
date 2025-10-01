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
import com.orderagentservice.order.service.utg.menu.MenuNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

interface BackSelectStrategy {
    fun execute(context: UtgContext, menuNodeId: String, uiList: List<UiComponentDto>): String
}

@Component(StrategyType.IN_BACK)
class DefaultBackSelectStrategy @Autowired constructor(
    private val notificationService: NotificationService,
    private val menuNodeIntegrator: MenuNodeIntegrator,
    private val screenNodeIntegrator: ScreenNodeIntegrator,
    private val comparatorManager: ComparatorManager,
    private val backAgent: BackAgent,
    private val logService: LogService
) : BackSelectStrategy {
    private val log = logger()

    override fun execute(
        context: UtgContext,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): String {
        val kioskId = context.kioskId

        //캐시된 것이 있는 지 확인
        val backUi = cacheBackUi(context, uiList)

        //노드 생성
        val creationResult = menuNodeIntegrator.integrateBackNode(backUi, menuNodeId, context)

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

@Component(StrategyType.EX_BACK)
class NoneBackSelectStrategy : BackSelectStrategy {
    override fun execute(context: UtgContext, menuNodeId: String, uiList: List<UiComponentDto>): String {
        return ""
    }
}
