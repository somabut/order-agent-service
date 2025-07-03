package com.orderagentservice.order

import com.orderagentservice.agent.ActionAgent
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.global.model.dto.RatioCoordinate
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.*
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.service.UiGraphService
import com.orderagentservice.order.service.UtgInitializeService
import com.orderagentservice.order.util.UiExtractorManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any


@ExtendWith(MockitoExtension::class)
class UmJunSikUtgInitializeServiceTest (
) {
    @Mock
    private lateinit var uiGraphService: UiGraphService

    @Mock
    private lateinit var uiExtractorManager: UiExtractorManager

    @Mock
    private lateinit var actionAgent: ActionAgent

    @InjectMocks
    private lateinit var utgInitializeService: UtgInitializeService

    @Test
    fun `주어진 메뉴를 기반으로 UTG를 생성한다`() {
        //given: mock 초기화
        val url = "https://test.com"
        val menuInfoList = listOf(
            MenuInfoDto(title = "크리스피 클래식", options = listOf(), category = "햄버거"),
            MenuInfoDto(title = "크리스퍼 팩", options = listOf(), category = "햄버거"),
            MenuInfoDto(title = "빅맥", options = listOf(), category = "햄버거"),
            MenuInfoDto(title = "핫 토마토 모짜볼", options = listOf(), category = "사이드"),
            MenuInfoDto(title = "바삭킹", options = listOf(), category = "사이드"),
            MenuInfoDto(title = "코코넛슈림프", options = listOf(), category = "사이드"),
            MenuInfoDto(title = "칠리소스", options = listOf(), category = "소스"),
            MenuInfoDto(title = "시즈닝", options = listOf(), category = "소스"),
            MenuInfoDto(title = "사이드", options = listOf(), category = "사이드"),
            MenuInfoDto(title = "소스", options = listOf(), category = "기타"),
            MenuInfoDto(title = "음료", options = listOf(), category = "음료")
        )
        val paymentList = listOf<PaymentInfoDto>()

        val omniUiComponents = listOf(
            OmniUiComponentDto(contents = listOf("크리스피 클래식 5600원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.145, 0.310, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("크리스퍼 팩 7900원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.212, 0.485, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("빅맥 7900원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.212, 0.485, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("통모짜와퍼 8200원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.398, 0.605, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("핫 토마토 모짜볼 4800원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.298, 0.572, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("바삭킹 4000원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.269, 0.231, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("군만두 4000원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.269, 0.231, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("코코넛슈림프 3600원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.357, 0.698, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("칠리소스 700원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.401, 0.633, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("파마산 치즈 600원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.289, 0.914, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("시즈닝 600원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.289, 0.914, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("사이드"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.289, 0.234, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("소스"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.289, 0.234, 0.310, 0.310), width = 100, height = 50)),
            OmniUiComponentDto(contents = listOf("음료"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.115, 0.234, 0.310, 0.310), width = 100, height = 50))
        )

        `when`(uiGraphService.saveNode(any())).thenReturn(UiEntity(
            isNext = true,
            x = -1, y = -1,
            url = "url",
            title = "title"
        ))
        doNothing().`when`(uiGraphService).saveRel(any(), any(), any<NodeRelation>())

        `when`(actionAgent.determineAction(any(), any())).thenAnswer { invocation ->
            val menuDto = invocation.arguments[0] as MenuInfoDto
            if (menuDto.category == menuDto.title) {
                AgentActionDto(goNext = true, score = 0.9F, coordinate = listOf(1, 2), title = "이동")
            } else {
                AgentActionDto(goNext = false, score = 0.9F, coordinate = listOf(3, 4), title = "정지")
            }
        }

        `when`(uiExtractorManager.queryUiExtractor(any())).thenReturn(omniUiComponents)

        val result = utgInitializeService.initializeGraph("moodTRBL", url, menuInfoList, paymentList)
    }
}