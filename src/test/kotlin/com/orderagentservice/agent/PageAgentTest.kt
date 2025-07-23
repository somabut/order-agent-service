package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import org.assertj.core.api.Assertions
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
            LlmUiComponentDto(x = 228, y = 31, title = "BURGER"),
            LlmUiComponentDto(x = 316, y = 31, title = "KING"),
            LlmUiComponentDto(x = 69, y = 107, title = "추천메뉴"),
            LlmUiComponentDto(x = 198, y = 106, title = "오리지널스웅맥시멈"),
            LlmUiComponentDto(x = 327, y = 107, title = "프리미엄"),
            LlmUiComponentDto(x = 454, y = 107, title = "와퍼오주니어"),
            LlmUiComponentDto(x = 69, y = 174, title = "치킨요슈림프버기"),
            LlmUiComponentDto(x = 198, y = 174, title = "올데이킹요킹모님"),
            LlmUiComponentDto(x = 326, y = 174, title = "사이드"),
            LlmUiComponentDto(x = 454, y = 174, title = "음료&디저"),
            LlmUiComponentDto(x = 140, y = 307, title = "통모짜와퍼"),
            LlmUiComponentDto(x = 197, y = 307, title = "라지세트"),
            LlmUiComponentDto(x = 136, y = 330, title = "통모찌와퍼"),
            LlmUiComponentDto(x = 216, y = 331, title = "프렌치프라이니"),
            LlmUiComponentDto(x = 296, y = 330, title = "코키콜라니"),
            LlmUiComponentDto(x = 216, y = 450, title = "프렌치프리이R"),
            LlmUiComponentDto(x = 297, y = 449, title = "코카콜라R"),
            LlmUiComponentDto(x = 138, y = 478, title = "9600원"),
            LlmUiComponentDto(x = 139, y = 546, title = "통모파와피"),
            LlmUiComponentDto(x = 137, y = 568, title = "통모피와퍼"),
            LlmUiComponentDto(x = 26, y = 672, title = "주문"),
            LlmUiComponentDto(x = 261, y = 694, title = "확인"),
            LlmUiComponentDto(x = 393, y = 753, title = "금맥"),
            LlmUiComponentDto(x = 394, y = 790, title = "총결제금액"),
            LlmUiComponentDto(x = 435, y = 901, title = "주문하기")
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
            LlmUiComponentDto(x = 145, y = 310, title = "사이드메뉴"),
            LlmUiComponentDto(x = 145, y = 310, title = "프렌치프리이나"),
            LlmUiComponentDto(x = 145, y = 310, title = "코카콜라R"),
            LlmUiComponentDto(x = 145, y = 310, title = "스프라이트 "),
            LlmUiComponentDto(x = 145, y = 310, title = "주문금액"),
            LlmUiComponentDto(x = 145, y = 310, title = "완료"),
            LlmUiComponentDto(x = 145, y = 310, title = "12800원"),
            LlmUiComponentDto(x = 145, y = 310, title = "21치즈스틱"),
            LlmUiComponentDto(x = 289, y = 234, title = "프렌치프라이R"),
            LlmUiComponentDto(x = 115, y = 234, title = "취소")
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