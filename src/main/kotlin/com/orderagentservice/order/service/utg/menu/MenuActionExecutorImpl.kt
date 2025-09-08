package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.NodeCreationResult
import com.orderagentservice.order.model.dto.UiComponentParams
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.ScreenNodeGenerator
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.WordSimilarityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuActionExecutorImpl @Autowired constructor(
    private val wordSimilarityService: WordSimilarityService,
    private val notificationService: NotificationService,
    private val menuNodeGenerator: MenuNodeGenerator,
    private val screenNodeGenerator: ScreenNodeGenerator,
    private val uiDetectorManager: UiDetectorManager,
    private val backAgent: BackAgent,
    private val graphService: UiGraphService,
    private val logService: LogService
) : MenuActionExecutor {
    private val log = logger()

    override fun selectCategory(
        context: GraphContext,
        menuDto: MenuInfoDto,
        uiList: List<UiComponentDto>
    ): NodeCreationResult {
        val matchDto = wordSimilarityService.findBestMatch(menuDto.category, uiList)

        //노드 생성
        val nodeCreationResult = menuNodeGenerator.createCategoryNode(matchDto, menuDto.category, context)
        context.lastNodeId = nodeCreationResult.nodeId

        //현재 카테고리 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(matchDto.x, matchDto.y, matchDto.title))

        return nodeCreationResult
    }

    override fun selectMenu(
        context: GraphContext,
        menuDto: MenuInfoDto,
        uiList: List<UiComponentDto>
    ): String {
        val matchDto = wordSimilarityService.findBestMatch(menuDto.title, uiList)

        //노드 생성
        val creationResult = menuNodeGenerator.createMenuNode(matchDto, menuDto.title, context)
        val nodeId = creationResult.nodeId

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeGenerator.linkNode(
            kioskId = context.kioskId,
            nodeId = nodeId, screenNodeId = context.screenNodeId,
            uiComponentParams = creationResult.uiComponentParams,
        )

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
        val llmOptList = uiDetectorManager.getUiComponents(context).uiElements

        for (opt in menuDto.options) {
            val matchDto = wordSimilarityService.findBestMatch(opt, llmOptList)

            //노드 생성
            val creationResult = menuNodeGenerator.createOptionNode(matchDto, opt, menuNodeId, context)

            //match 노드와 관계, screen 노드와 관계 연결
            screenNodeGenerator.linkNode(
                kioskId = context.kioskId,
                nodeId = creationResult.nodeId, screenNodeId = context.screenNodeId,
                uiComponentParams = creationResult.uiComponentParams,
            )
        }
    }

    override fun selectBack(
        context: GraphContext,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): String {
        val kioskId = context.kioskId

        //캐시된 것이 있는 지 확인
        val backUi = cacheBackUi(context, uiList)

        //노드 생성
        val creationResult = menuNodeGenerator.createBackNode(backUi, menuNodeId, context)

        //match 노드와 관계, screen 노드와 관계 연결
        screenNodeGenerator.linkNode(
            kioskId = context.kioskId,
            nodeId = creationResult.nodeId, screenNodeId = context.screenNodeId,
            uiComponentParams = creationResult.uiComponentParams,
        )

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        logService.printLog(UtgProcessLog(
            kioskId = kioskId,
            message = "돌아가는 좌표를 클릭중입니다. 좌표: ${backUi.coordinate}"
        ))
        val (x, y) = backUi.coordinate
        notificationService.sendActionCommand(kioskId, CoordinateDto(x, y, backUi.title))

        return creationResult.nodeId
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
            val creationResult = menuNodeGenerator.createModalNode(
                context = context,
                matchDto = matchDto,
                menuDto = menuDto,
                menuNodeId = nodeId
            )
            nodeId = creationResult.nodeId

            //match 노드와 관계, screen 노드와 관계 연결
            screenNodeGenerator.linkNode(
                kioskId = context.kioskId,
                nodeId = nodeId, screenNodeId = context.screenNodeId,
                uiComponentParams = creationResult.uiComponentParams,
            )
        }

        //다음으로 이동
        val nextUiList = uiDetectorManager.getUiComponents(context).uiElements
        nodeId = selectBack(context, nodeId, nextUiList)

        return nodeId
    }

    private fun cacheBackUi(context: GraphContext, uiList: List<UiComponentDto>): AgentBackDto {
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
            val matchDto = wordSimilarityService.findBestMatch(context.menuBackUi!!, uiList)
            backUi = AgentBackDto(
                score = 1.0F,
                coordinate = listOf(matchDto.x, matchDto.y), bbox = listOf(matchDto.minX, matchDto.minY, matchDto.maxX, matchDto.maxY),
                title = matchDto.title
            )
        }

        return backUi
    }
}