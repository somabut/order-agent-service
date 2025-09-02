package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.AgentUiDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.type.ExtractType
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.WordSimilarityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuActionExecutorImpl @Autowired constructor(
    private val wordSimilarityService: WordSimilarityService,
    private val notificationService: NotificationService,
    private val nodeGenerator: MenuNodeGenerator,
    private val uiDetectorManager: UiDetectorManager,
    private val backAgent: BackAgent,
    private val graphService: UiGraphService,
    private val logService: LogService
) : MenuActionExecutor {

    override fun selectCategory(
        context: GraphContext,
        menuDto: MenuInfoDto,
        uiList: List<UiComponentDto>
    ) {
        val matchDto = wordSimilarityService.findBestMatch(menuDto.category, uiList)

        //노드 생성
        val nodeId = nodeGenerator.createCategoryNode(matchDto, context)
        context.lastNodeId = nodeId

        //현재 카테고리 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(matchDto.x, matchDto.y, matchDto.title))
    }

    override fun selectMenu(
        context: GraphContext,
        menuDto: MenuInfoDto,
        uiList: List<UiComponentDto>
    ): String {
        val matchDto = wordSimilarityService.findBestMatch(menuDto.title, uiList)

        //노드 생성
        val nodeId = nodeGenerator.createMenuNode(matchDto, context)

        //현재 메뉴 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(matchDto.x, matchDto.y, matchDto.title))

        return nodeId
    }

    override fun selectOption(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
    ) {
        //메뉴의 옵션 노드 추가
        val llmOptList = uiDetectorManager.getUiComponents(context, ExtractType.SOM)

        for (opt in menuDto.options) {
            val matchDto = wordSimilarityService.findBestMatch(opt, llmOptList)

            //노드 생성
            nodeGenerator.createOptionNode(matchDto, menuNodeId, context)
        }
    }

    override fun selectBack(
        context: GraphContext,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): String {
        val kioskId = context.kioskId

        //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
        val agentUiList = uiList
        val backAction = backAgent.determineAction(agentUiList)

        //노드 생성
        val backNodeId = nodeGenerator.createBackNode(backAction, menuNodeId, context)

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        logService.printLog(UtgProcessLog(
            kioskId = kioskId,
            message = "돌아가는 좌표를 클릭중입니다. 좌표: ${backAction.coordinate}"
        ))
        notificationService.sendActionCommand(kioskId, CoordinateDto(backAction.coordinate[0], backAction.coordinate[1], backAction.title))

        return backNodeId
    }

    override fun selectModal(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): String {
        //메뉴를 다시 선택(모달 처리)해야 할 수도 있으므로 클릭
        var nodeId = menuNodeId
        val matchDto = wordSimilarityService.findBestMatch(menuDto.title, uiList)

        //모달인 경우에만 모달노드 저장과 액션수행
        if (matchDto.score >= 0.65) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(x = matchDto.x, y = matchDto.y, title = matchDto.title))
            graphService.changeTitle(nodeId, context.kioskId, "modal:${menuDto.title}")

            //모달 노드 저장
            nodeId = nodeGenerator.createModalNode(
                context = context,
                matchDto = matchDto,
                menuDto = menuDto,
                menuNodeId = nodeId
            )
        }

        //다음으로 이동
        val nextUiList = uiDetectorManager.getUiComponents(context, ExtractType.SOM)
        nodeId = selectBack(context, nodeId, nextUiList)

        return nodeId
    }
}