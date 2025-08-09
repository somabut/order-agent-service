package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.global.service.WordSimilarityService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.GraphService
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenuUtgService @Autowired constructor(
    private val placeUtgService: PlaceUtgService,
    private val backAgent: BackAgent,
    private val wordSimilarityService: WordSimilarityService,
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val graphService: GraphService
) : AbstractMenuUtgService(
    backAgent = backAgent,
    wordSimilarityService = wordSimilarityService,
    uiExtractorManager = uiExtractorManager,
    notificationService = notificationService,
    graphService = graphService
) {
    private val log = logger()

    @Transactional
    fun initializeGraph(context: GraphContext, menuList: List<MenuInfoDto>) {
        log.info("메뉴 utg 생성 시작")
        val startTime = System.nanoTime()

        // root, station노드 초기화
        setupNode(context)

        //포장/매장 찾기
        placeUtgService.initializeGraph(context)

        //루프를 돌며 메뉴들을 모두 탐색
        navigateMenus(context, menuList)

        //포장/매장 찾기
        if (context.isPlaceDetermined == false) {
            placeUtgService.initializeGraph(context)
        }

        val endTime = System.nanoTime()
        log.info("메뉴 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    @Transactional
    fun updateGraph(context: GraphContext, categoryMenuList: List<MenuInfoDto>) {
        log.info("메뉴 utg 업데이트 시작")
        val startTime = System.nanoTime()

        val nowNodeId = graphService.findRoot(context.kioskId).id
        context.lastNodeId = nowNodeId

        //카테고리로 이동
        moveCategory(context, categoryMenuList)

        //해당 페이지에서 메뉴 탐색
        navigateMenus(context, categoryMenuList)

        val endTime = System.nanoTime()
        log.info("메뉴 utg 업데이트 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    private fun moveCategory(context: GraphContext, categoryMenuList: List<MenuInfoDto>) {
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

    private fun setupNode(context: GraphContext) {
        log.info("root노드와 station노드를 초기화합니다.")
        val kioskId = context.kioskId
        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "root"
        )
        context.lastNodeId = graphService.saveNode(rootUiDto).id

        val stationNode = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = kioskId,
            title = "station"
        )
        context.stationNodeId = graphService.saveNode(stationNode).id

        graphService.saveRel(context.lastNodeId!!, context.stationNodeId!!, NodeRelation.PATH_TO)
    }
}