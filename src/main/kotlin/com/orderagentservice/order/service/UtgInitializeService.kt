package com.orderagentservice.order.service

import com.orderagentservice.agent.BackAgent
import com.orderagentservice.agent.MenuAgent
import com.orderagentservice.agent.StepAgent
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.agent.model.dto.UiActionDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.OmniUiComponentDto
import com.orderagentservice.order.model.dto.PaymentInfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.http.client.reactive.HttpReactiveClientProperties
import org.springframework.stereotype.Service
import java.io.File

@Service
class UtgInitializeService @Autowired constructor(
    private val menuAgent: MenuAgent,
    private val backAgent: BackAgent,
    private val uiGraphService: UiGraphService,
    private val uiExtractorManager: UiExtractorManager
) {
    private val log = logger()

    fun initializeGraph(url: String, menuList: List<MenuInfoDto>, paymentList: List<PaymentInfoDto>): List<UiActionDto> {
        log.info("UTG 생성 시작")
        val startTime = System.nanoTime()

        var isNext = true
        val actionList = mutableListOf<UiActionDto>()
        val rootUiDto = UiDto(
            isNext = true,
            x = -1,
            y = -1,
            url = url,
            title = "root"
        )
        var preNode = uiGraphService.saveNode(rootUiDto)
        var preDto: MenuInfoDto? = null
        var llmUiList = mutableListOf<LlmUiComponentDto>()
        var menuIndex = 0
        while (true) {
            val menuDto = menuList[menuIndex]

            //이전 카테고리랑 다르면 이미지 파싱
            if (isNext == true) {
                llmUiList.clear()
                llmUiList = getUiComponents()
                isNext = false
                continue
            }
            println(llmUiList)
            log.info("진행 중인 노드 menu: ${menuDto.title}, category: ${menuDto.category}")

            val action = menuAgent.determineAction(menuDto, llmUiList)
            actionList.add(action)
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

            //메뉴의 옵션 노드 추가
            //이후에 다시 원래 페이지로 돌아가야 하므로 backAgent를 통해 이전 페이지로 돌아가기
            val llmOptList = getUiComponents()
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

            //TODO(sse를 통해 클라이언트에게 돌아가는 좌표 클릭하도록 하기)

            //isNext이고 이전이랑 카테고리 다른 경우. 다른 페이지로 가야하는 경우
            if (isNext && ((preDto != null) and (preDto!!.category != menuDto.category))) {
                uiGraphService.saveRel(preNode.id, entity.id, NodeRelation.PATH_TO)
                uiGraphService.saveRel(entity.id, preNode.id, NodeRelation.PATH_TO)
                preNode = entity

                //TODO(sse를 통해 클라이언트에게 현재 메뉴 좌표 클릭하도록 하기)
            } else {
                uiGraphService.saveRel(preNode.id, entity.id, NodeRelation.HAS_TO)
                menuIndex++
            }
            preDto = menuDto

            if (menuIndex == menuList.size) break
        }

        val endTime = System.nanoTime()
        log.info("수행시간: ${(endTime - startTime) / 1000000}ms")
        return actionList
    }

    private fun getUiComponents(): MutableList<LlmUiComponentDto> {
        //TODO(sse를 통해 클라이언트에게 캡쳐 이미지 받기)
        val image = File("")
        val uiComponents = uiExtractorManager.queryUiExtractor(image)

        //옴니파서에게 받은 이미지 적절히 변환
        val llmUiList = mutableListOf<LlmUiComponentDto>()
        for (ele in uiComponents) {
            val width = ele.bbox.width
            val height = ele.bbox.height
            var title = ""
            for (str in ele.contents) {
                title += str
            }

            val pixelCoordinate = ele.bbox.coordinate.toPixel(width, height)
            val cord = pixelCoordinate.getCenter()
            llmUiList.add(LlmUiComponentDto(
                x = cord.first,
                y = cord.second,
                title = title
            ))
        }
        return llmUiList
    }
}