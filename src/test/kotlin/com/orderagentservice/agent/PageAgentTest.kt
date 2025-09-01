package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.AgentUiDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PageAgentTest @Autowired constructor(
    private val pageAgent: PageAgent
) {
    @Test
    fun `현재 페이지인지 메타를 보고 미포함 관계를 판단한다`() {
        //given: ui list와 메타
        val uiList = listOf(
            AgentUiDto(x = 228, y = 31, title = "BURGER"),
            AgentUiDto(x = 316, y = 31, title = "KING"),
            AgentUiDto(x = 69, y = 107, title = "추천메뉴"),
            AgentUiDto(x = 198, y = 106, title = "오리지널스웅맥시멈"),
            AgentUiDto(x = 327, y = 107, title = "프리미엄"),
            AgentUiDto(x = 454, y = 107, title = "와퍼오주니어"),
            AgentUiDto(x = 69, y = 174, title = "치킨요슈림프버기"),
            AgentUiDto(x = 198, y = 174, title = "올데이킹요킹모님"),
            AgentUiDto(x = 326, y = 174, title = "사이드"),
            AgentUiDto(x = 454, y = 174, title = "음료&디저"),
            AgentUiDto(x = 140, y = 307, title = "통모짜와퍼"),
            AgentUiDto(x = 197, y = 307, title = "라지세트"),
            AgentUiDto(x = 136, y = 330, title = "통모찌와퍼"),
            AgentUiDto(x = 216, y = 331, title = "프렌치프라이니"),
            AgentUiDto(x = 296, y = 330, title = "코키콜라니"),
            AgentUiDto(x = 216, y = 450, title = "프렌치프리이R"),
            AgentUiDto(x = 297, y = 449, title = "코카콜라R"),
            AgentUiDto(x = 138, y = 478, title = "9600원"),
            AgentUiDto(x = 139, y = 546, title = "통모파와피"),
            AgentUiDto(x = 137, y = 568, title = "통모피와퍼"),
            AgentUiDto(x = 26, y = 672, title = "주문"),
            AgentUiDto(x = 261, y = 694, title = "확인"),
            AgentUiDto(x = 393, y = 753, title = "금맥"),
            AgentUiDto(x = 394, y = 790, title = "총결제금액"),
            AgentUiDto(x = 435, y = 901, title = "주문하기")
        )
        val metaList = listOf(
            "프렌치프라이(R)", "프렌치 프라이(L)", "21치즈스틱", "코카콜라(R)", "스프라이트(R)"
        )   //3개, 60%

        //when: llm에게 질의 한다
        val response = pageAgent.determineAction(metaList, uiList)
        println(response)

        //then: 올바른 답변 반환
        assertThat(response.contain).isEqualTo(false)
    }

    @Test
    fun `현재 페이지인지 메타를 보고 포함 관계를 판단한다`() {
        //given: ui list와 메타
        val uiList = listOf(
            AgentUiDto(x = 145, y = 310, title = "사이드메뉴"),
            AgentUiDto(x = 145, y = 310, title = "프렌치프리이나"),
            AgentUiDto(x = 145, y = 310, title = "코카콜라R"),
            AgentUiDto(x = 145, y = 310, title = "스프라이트 "),
            AgentUiDto(x = 145, y = 310, title = "주문금액"),
            AgentUiDto(x = 145, y = 310, title = "완료"),
            AgentUiDto(x = 145, y = 310, title = "12800원"),
            AgentUiDto(x = 145, y = 310, title = "21치즈스틱"),
            AgentUiDto(x = 289, y = 234, title = "프렌치프라이R"),
            AgentUiDto(x = 115, y = 234, title = "취소")
        )
        val metaList = listOf(
            "프렌치프라이(R)", "프렌치 프라이(L)", "21치즈스틱", "코카콜라(R)", "스프라이트(R)"
        )

        //when: llm에게 질의 한다
        val response = pageAgent.determineAction(metaList, uiList)
        println(response)

        //then: 올바른 답변 반환
        assertThat(response.contain).isEqualTo(true)
    }
}