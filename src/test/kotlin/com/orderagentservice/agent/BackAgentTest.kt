package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.UiComponentDto
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BackAgentTest @Autowired constructor(
    private val backAgent: BackAgent
) {
    @Test
    fun `옵션 선택을 완료하기 위해 상호작용해야 하는 UI를 찾는다`() {
        //given: ui list
        val uiList = listOf(
            UiComponentDto(x = 145, y = 310, title = "프렌치 프라이 5600원"),
            UiComponentDto(x = 212, y = 485, title = "너겟킹 7900원"),
            UiComponentDto(x = 398, y = 605, title = "리얼 어니언링 8200원"),
            UiComponentDto(x = 125, y = 732, title = "다음으로"),
            UiComponentDto(x = 362, y = 298, title = "코카콜라 7500원"),
            UiComponentDto(x = 274, y = 847, title = "코카콜라 제로 9500원"),
            UiComponentDto(x = 458, y = 415, title = "취소"),
            UiComponentDto(x = 107, y = 921, title = "미밋메이드 오렌지 8800원"),
            UiComponentDto(x = 376, y = 534, title = "스프라이트 8500원")
        )

        //when: llm에게 질의
        val response = backAgent.determineBack(uiList)

        //then: 올바른 액션 반환
        assertThat(response.title).isEqualTo("다음으로")
        assertThat(response.coordinate[0]).isEqualTo(125)
        assertThat(response.coordinate[1]).isEqualTo(732)
    }

    @Test
    fun `중복되는 완료 UI중에 적절한 것을 찾는다`() {
        //given: ui list
        val uiList = listOf(
            UiComponentDto(x = 145, y = 310, title = "프렌치 프라이 5600원"),
            UiComponentDto(x = 212, y = 485, title = "너겟킹 7900원"),
            UiComponentDto(x = 398, y = 605, title = "리얼 어니언링 8200원"),
            UiComponentDto(x = 125, y = 732, title = "결제하기"),
            UiComponentDto(x = 67, y = 99, title = "선택완료"),
            UiComponentDto(x = 362, y = 298, title = "코카콜라 7500원"),
            UiComponentDto(x = 274, y = 847, title = "코카콜라 제로 9500원"),
            UiComponentDto(x = 458, y = 415, title = "취소"),
            UiComponentDto(x = 107, y = 921, title = "미밋메이드 오렌지 8800원"),
            UiComponentDto(x = 376, y = 534, title = "스프라이트 8500원")
        )

        //when: llm에게 질의
        val response = backAgent.determineBack(uiList)
        println(response)

        assertThat(response.title).isEqualTo("선택완료")
        assertThat(response.coordinate[0]).isEqualTo(67)
        assertThat(response.coordinate[1]).isEqualTo(99)
    }
}