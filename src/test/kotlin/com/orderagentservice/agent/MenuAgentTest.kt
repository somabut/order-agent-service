package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MenuAgentTest @Autowired constructor(
    private val menuAgent: MenuAgent
) {
    private val uiList = mutableListOf(
        UiComponentDto(x = 145, y = 310, title = "크리스퍼 클래식 5600원"),
        UiComponentDto(x = 212, y = 485, title = "통모짜와퍼 7900원"),
        UiComponentDto(x = 398, y = 605, title = "BBQ통모짜 와퍼 8200원"),
        UiComponentDto(x = 125, y = 732, title = "매장"),
        UiComponentDto(x = 362, y = 298, title = "포장"),
        UiComponentDto(x = 68, y = 77, title = "오리지럴&맥시멈"),
        UiComponentDto(x = 274, y = 847, title = "처음으로"),
        UiComponentDto(x = 458, y = 415, title = "주문하기"),
        UiComponentDto(x = 107, y = 921, title = "사이드"),
        UiComponentDto(x = 376, y = 534, title = "추천메뉴")
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
            title = "오리지널스&맥시멈",
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

    @Test
    fun `모달내부에서 다시 메뉴를 선택한다`() {
        val optUiList = listOf(
            UiComponentDto(x = 42, y = 31, title = "사이드"),
            UiComponentDto(x = 73, y = 31, title = "메뉴"),
            UiComponentDto(x = 76, y = 142, title = "불끈버거 맥시멈"),
            UiComponentDto(x = 197, y = 142, title = "불끈버거 맥시멈 세트"),
            UiComponentDto(x = 317, y = 142, title = "불끈버거 맥시멈 라지세트"),
            UiComponentDto(x = 197, y = 163, title = "+800원"),
            UiComponentDto(x = 319, y = 163, title = "+300뭔"),
            UiComponentDto(x = 34, y = 432, title = "음료"),
            UiComponentDto(x = 75, y = 543, title = "코카콜라R"),
            UiComponentDto(x = 196, y = 543, title = "스프라이트R"),
            UiComponentDto(x = 198, y = 564, title = "100원"),
            UiComponentDto(x = 56, y = 835, title = "총주문금액"),
            UiComponentDto(x = 464, y = 835, title = "13500원"),
            UiComponentDto(x = 138, y = 884, title = "취소"),
            UiComponentDto(x = 383, y = 884, title = "완료")
        )
        val menuDto = MenuInfoDto(
            title = "불끈버거 맥시멈",
            options = listOf(),
            category = "맥시멈"
        )

        //when: llm에게 질의한다
        val response = menuAgent.determineAction(menuDto, optUiList)
        println(response)
    }

    @Test
    fun `옵션을 선택한다`() {
        val optUiList = listOf(
            UiComponentDto(x = 42, y = 31, title = "사이드"),
            UiComponentDto(x = 73, y = 31, title = "메뉴"),
            UiComponentDto(x = 76, y = 142, title = "빽스치노"),
            UiComponentDto(x = 197, y = 142, title = "딸기빽스치노"),
            UiComponentDto(x = 317, y = 142, title = "녹차빽스치노"),
            UiComponentDto(x = 197, y = 163, title = "초코빽스치노"),
            UiComponentDto(x = 319, y = 163, title = "+300뭔"),
            UiComponentDto(x = 34, y = 432, title = "음료"),
            UiComponentDto(x = 75, y = 543, title = "고카콜라R"),
            UiComponentDto(x = 196, y = 543, title = "스프라이트R"),
            UiComponentDto(x = 198, y = 564, title = "100원"),
            UiComponentDto(x = 56, y = 835, title = "총주문금액"),
            UiComponentDto(x = 464, y = 835, title = "13500원"),
            UiComponentDto(x = 138, y = 884, title = "취소"),
            UiComponentDto(x = 383, y = 884, title = "완료")
        )
        val menuDto = MenuInfoDto(
            title = "빽스치노",
            options = listOf(),
            category = "빽스치노"
        )

        //when: llm에게 질의한다
        val response = menuAgent.determineAction(menuDto, optUiList)

        assertThat(response.title).isEqualTo("빽스치노")
        println(response)
    }
}