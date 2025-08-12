package com.orderagentservice.order.service.utg.menu

import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.service.graph.GraphService
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MenuUtgService @Autowired constructor(
    private val menuNavigator: MenuNavigator,
    private val placeUtgService: PlaceUtgService,
    private val graphService: GraphService
)  {
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
        menuNavigator.navigateMenus(context, menuList)

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
        menuNavigator.moveCategory(context, categoryMenuList)

        //해당 페이지에서 메뉴 탐색
        menuNavigator.navigateMenus(context, categoryMenuList)

        val endTime = System.nanoTime()
        log.info("메뉴 utg 업데이트 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
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