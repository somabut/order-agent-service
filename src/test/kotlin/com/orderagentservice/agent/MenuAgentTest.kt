package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MenuAgentTest @Autowired constructor(
    private val menuAgent: MenuAgent
) {
    private val uiList = mutableListOf(
        LlmUiComponentDto(x = 145, y = 310, title = "크리스퍼 클래식 5600원"),
        LlmUiComponentDto(x = 212, y = 485, title = "통모짜와퍼 7900원"),
        LlmUiComponentDto(x = 398, y = 605, title = "BBQ통모짜 와퍼 8200원"),
        LlmUiComponentDto(x = 125, y = 732, title = "매장"),
        LlmUiComponentDto(x = 362, y = 298, title = "포장"),
        LlmUiComponentDto(x = 68, y = 77, title = "오리지럴&맥시멈"),
        LlmUiComponentDto(x = 274, y = 847, title = "처음으로"),
        LlmUiComponentDto(x = 458, y = 415, title = "주문하기"),
        LlmUiComponentDto(x = 107, y = 921, title = "사이드"),
        LlmUiComponentDto(x = 376, y = 534, title = "추천메뉴")
    )

    @Test
    fun `세트 메뉴를 고려하여 메뉴를 선택한다`() {
        val menuDto = MenuInfoDto(
            title = "크리스퍼 클래식 라지 세트",
            options = listOf(),
            category = "추천메뉴"
        )

        //when: llm에게 질의한다
        val response = menuAgent.determineAction(menuDto, uiList)

        //then: 올바른 답변을 반환한다
        assertThat(response.title).isEqualTo("크리스퍼 클래식 라지 세트")
        assertThat(response.coordinate[0]).isEqualTo(145)
        assertThat(response.coordinate[1]).isEqualTo(310)
    }

    @Test
    fun `카테고리를 고려하여 메뉴를 선택한다`() {
        val menuDto = MenuInfoDto(
            title = "불끈버거 맥시멈",
            options = listOf(),
            category = "오리지널스&맥시멈"
        )

        //when: llm에게 질의한다
        val response = menuAgent.determineAction(menuDto, uiList)

        //then: 올바른 답변을 반환한다
        assertThat(response.title).isEqualTo("오리지널스&맥시멈")
        assertThat(response.coordinate[0]).isEqualTo(68)
        assertThat(response.coordinate[1]).isEqualTo(77)
    }

    @Test
    fun `메뉴를 선택한다`() {
        val menuDto = MenuInfoDto(
            title = "크리스퍼 클래식",
            options = listOf(),
            category = "추천메뉴"
        )

        //when: llm에게 질의한다
        val response = menuAgent.determineAction(menuDto, uiList)

        //then: 올바른 답변을 반환한다
        assertThat(response.title).isEqualTo("크리스퍼 클래식")
        assertThat(response.coordinate[0]).isEqualTo(145)
        assertThat(response.coordinate[1]).isEqualTo(310)
    }
}