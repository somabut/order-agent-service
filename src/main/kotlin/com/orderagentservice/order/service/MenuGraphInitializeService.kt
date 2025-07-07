package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MenuGraphInitializeService @Autowired constructor(
    private val menuAgent: MenuAgent,
    private val backAgent: BackAgent,
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val uiGraphService: UiGraphService
) {
    private val log = logger()

    fun initializeGraph(url: String, kioskId: String, menuList: List<MenuInfoDto>): Pair<List<AgentActionDto>, UiEntity> {
        log.info("메뉴 utg 생성 시작")
        val startTime = System.nanoTime()

        var isNext = true
        val history = mutableListOf<AgentActionDto>()

        val rootUiDto = UiDto(
            isNext = true,
            x = -1,
            y = -1,
            url = url,
            title = "root"
        )
        var preNode = uiGraphService.saveNode(rootUiDto)
        var llmUiList = mutableListOf<LlmUiComponentDto>()
        var menuIndex = 0
        while (true) {
            val menuDto = menuList[menuIndex]

            //이전 카테고리랑 다르면 이미지 파싱
            if (isNext == true) {
                llmUiList.clear()
                llmUiList = uiExtractorManager.getUiComponents(kioskId)
                isNext = false
                continue
            }
            log.info("진행 중인 노드 menu: ${menuDto.title}, category: ${menuDto.category}")

            val action = menuAgent.determineAction(menuDto, llmUiList)
            history.add(action)
            log.info("go_next: ${action.goNext}, score: ${action.score}, coordinate: ${action.coordinate}, title: ${action.title}")

            if (action.score < 0.6) {
                log.info("낮은 액션 정확도 점수: ${action.score}")
                continue
            }

            isNext = action.goNext
            val entity = uiGraphService.saveNode(UiDto(
                isNext = isNext,
                x = action.coordinate[0],
                y = action.coordinate[1],
                title = action.title,
                url = url
            ))

            //sse를 통해 클라이언트에게 현재 메뉴 좌표 클릭하도록 하기
            notificationService.sendActionCommand(kioskId, action.coordinate)

            //메뉴의 옵션 노드 추가
            val llmOptList = uiExtractorManager.getUiComponents(kioskId)

            //TODO(감지되지 못한 옵션이 있는 경우 GPT 에게 질의)
            for (opt in menuDto.options) {
                for (ele in llmOptList) {

                    //TODO(감지되지 못했다면 GPT에게 질의하여 알아내고 options에서 제거)
                    if (ele.title.contains(opt) == false) {

                    }
                }
            }

            //다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
            val backAction = backAgent.determineBack(llmOptList)
            for (opt in menuDto.options) {
                val optAction = menuAgent.determineAction(MenuInfoDto(opt, listOf(), menuDto.title), llmOptList)

                val optEntity = uiGraphService.saveNode(UiDto(
                    isNext = false,
                    x = optAction.coordinate[0],
                    y = optAction.coordinate[1],
                    title = optAction.title,
                    url = url
                ))
                uiGraphService.saveRel(entity.id, optEntity.id, NodeRelation.OPT_TO)
                uiGraphService.saveRel(optEntity.id, entity.id, NodeRelation.BACK_TO)
            }

            //sse를 통해 클라이언트에게 돌아가는 좌표 클릭하도록 하기
            notificationService.sendActionCommand(kioskId, backAction.coordinate)

            //isNext이고 이전이랑 카테고리 다른 경우. 다른 페이지로 가야하는 경우
            if (isNext) {
                uiGraphService.saveRel(preNode.id, entity.id, NodeRelation.PATH_TO)
                uiGraphService.saveRel(entity.id, preNode.id, NodeRelation.PATH_TO)
                preNode = entity
            } else {
                uiGraphService.saveRel(preNode.id, entity.id, NodeRelation.HAS_TO)
                menuIndex++
            }

            if (menuIndex == menuList.size) break
        }

        val endTime = System.nanoTime()
        log.info("메뉴 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")

        return Pair(history, preNode)
    }
}