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
            LlmUiComponentDto(x = 145, y = 310, title = "크리스피 클래식 라지세트"),
            LlmUiComponentDto(x = 145, y = 310, title = "크리스피 클래식 세트"),
            LlmUiComponentDto(x = 145, y = 310, title = "크리스피 클래식"),
            LlmUiComponentDto(x = 145, y = 310, title = "6800원"),
            LlmUiComponentDto(x = 145, y = 310, title = "확인"),
            LlmUiComponentDto(x = 145, y = 310, title = "주문하기"),
            LlmUiComponentDto(x = 145, y = 310, title = "프리미엄"),
            LlmUiComponentDto(x = 145, y = 310, title = "오리지널스"),
            LlmUiComponentDto(x = 289, y = 234, title = "사이드"),
            LlmUiComponentDto(x = 115, y = 234, title = "음류 디저트"),
            LlmUiComponentDto(x = 28, y = 889, title = "결제하기")
        )
        val metaList = listOf(
            "프렌치프라이", "프렌치 프라이(L)", "21치즈스틱", "코카콜라", "스프라이트"
        )

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
            LlmUiComponentDto(x = 145, y = 310, title = "코카콜라나"),
            LlmUiComponentDto(x = 145, y = 310, title = "스프라이트 100원"),
            LlmUiComponentDto(x = 145, y = 310, title = "주문금액"),
            LlmUiComponentDto(x = 145, y = 310, title = "완료"),
            LlmUiComponentDto(x = 145, y = 310, title = "12800원"),
            LlmUiComponentDto(x = 145, y = 310, title = "21치즈스틱"),
            LlmUiComponentDto(x = 289, y = 234, title = "프렌치프라이ㄱ"),
            LlmUiComponentDto(x = 115, y = 234, title = "취소")
        )
        val metaList = listOf(
            "프렌치프라이", "프렌치 프라이(L)", "21치즈스틱", "코카콜라", "스프라이트"
        )

        //when: llm에게 질의 한다
        val response = pageAgent.determineAction(metaList, uiList)
        println(response)

        //then: 올바른 답변 반환
        assertThat(response.contain).isEqualTo(true)
    }
}