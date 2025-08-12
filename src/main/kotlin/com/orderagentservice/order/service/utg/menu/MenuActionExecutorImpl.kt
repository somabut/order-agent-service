package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.WordSimilarityService
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MenuActionExecutorImpl @Autowired constructor(
    private val wordSimilarityService: WordSimilarityService,
    private val notificationService: NotificationService,
    private val nodeGenerator: MenuNodeGenerator,
    private val uiExtractorManager: UiExtractorManager,
    private val backAgent: BackAgent,
) : MenuActionExecutor {
    private val log = logger()

    override fun selectCategory(
        context: GraphContext,
        menuDto: MenuInfoDto,
        uiList: List<UiComponentDto>
    ) {
        val coordinate = wordSimilarityService.findBestMatch(menuDto.category, uiList)
            .toCoordinateDto(menuDto.category)

        //노드 생성
        val nodeId = nodeGenerator.createCategoryNode(coordinate, context)
        context.lastNodeId = nodeId

        //현재 카테고리 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(coordinate.x, coordinate.y, coordinate.title))
    }

    override fun selectMenu(
        context: GraphContext,
        menuDto: MenuInfoDto,
        uiList: List<UiComponentDto>
    ): String {
        val coordinate = wordSimilarityService.findBestMatch(menuDto.title, uiList)
            .toCoordinateDto(menuDto.title)

        //노드 생성
        val nodeId = nodeGenerator.createMenuNode(coordinate, context)

        //현재 메뉴 좌표 클릭
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(coordinate.x, coordinate.y, coordinate.title))

        return nodeId
    }

    override fun selectOption(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
    ) {
        //메뉴의 옵션 노드 추가
        val kioskId = context.kioskId
        val llmOptList = uiExtractorManager.getUiComponents(kioskId)

        for (opt in menuDto.options) {
            val coordinate = wordSimilarityService.findBestMatch(opt, llmOptList)
                .toCoordinateDto(opt)

            //노드 생성
            nodeGenerator.createOptionNode(coordinate, menuNodeId, context)
        }
    }

    override fun selectBack(
        context: GraphContext,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): String {
        val kioskId = context.kioskId

        //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
        val backAction = backAgent.determineAction(uiList)

        //노드 생성
        val backNodeId = nodeGenerator.createBackNode(backAction, menuNodeId, context)

        //sse를 통해 클라이언트에게 원래 페이지로 돌아가는 좌표 클릭하도록 하기
        log.info("돌아가는 좌표를 클릭중입니다. 좌표: ${backAction.coordinate}")
        notificationService.sendActionCommand(kioskId, CoordinateDto(backAction.coordinate[0], backAction.coordinate[1], backAction.title))

        return backNodeId
    }

    override fun selectModal(
        context: GraphContext,
        menuDto: MenuInfoDto,
        menuNodeId: String,
        uiList: List<UiComponentDto>
    ): String {
        //메뉴를 다시 선택해야 할 수도 있으므로 클릭
        var nodeId = menuNodeId
        val matchDto = wordSimilarityService.findBestMatch(menuDto.title, uiList)
        if (matchDto.score >= 0.6) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(x = matchDto.x, y = matchDto.y, title = matchDto.title))
        }

        //노드명 변경
        nodeId = nodeGenerator.createModalNode(
            context = context,
            matchDto = matchDto,
            menuDto = menuDto,
            menuNodeId = nodeId
        )

        //다음으로 이동
        val nextUiList = uiExtractorManager.getUiComponents(context.kioskId)
        nodeId = selectBack(context, nodeId, nextUiList)

        return nodeId
    }
}