package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PlaceAgentTest @Autowired constructor(
    private val placeAgent: PlaceAgent
) {
    @Test
    fun `포장 매장을 선택하는 UI를 찾아낸다`() {
        //given: ui list
        val uiList = listOf(
            LlmUiComponentDto(x = 145, y = 310, title = "먹고가기"),
            LlmUiComponentDto(x = 212, y = 485, title = "너겟킹 7900원"),
            LlmUiComponentDto(x = 398, y = 605, title = "리얼 어니언링 8200원"),
            LlmUiComponentDto(x = 125, y = 732, title = "포장"),
            LlmUiComponentDto(x = 362, y = 298, title = "코카콜라 7500원"),
            LlmUiComponentDto(x = 274, y = 847, title = "코카콜라 제로 9500원"),
            LlmUiComponentDto(x = 458, y = 415, title = "취소 8900원"),
            LlmUiComponentDto(x = 107, y = 921, title = "미밋메이드 오렌지 8800원"),
            LlmUiComponentDto(x = 376, y = 534, title = "스프라이트 8500원")
        )

        //when: llm에게 질의
        val response = placeAgent.determineAction(uiList)

        //then: 올바른 액션 반환
        for (dto in response) {
            if (dto.title == "매장") {
                assertThat(dto.coordinate[0]).isEqualTo(145)
                assertThat(dto.coordinate[1]).isEqualTo(310)
            } else if (dto.title == "포장") {
                assertThat(dto.coordinate[0]).isEqualTo(125)
                assertThat(dto.coordinate[1]).isEqualTo(732)
            } else {
                //실패
                assertThat(1).isEqualTo(2)
            }
        }
    }

    @Test
    fun `포장 매장 UI가 없으면 미리 정의된 응답을 반환한다`() {
        //given: ui list
        val uiList = listOf(
            LlmUiComponentDto(x = 212, y = 485, title = "완료"),
            LlmUiComponentDto(x = 398, y = 605, title = "주문하기"),
            LlmUiComponentDto(x = 362, y = 298, title = "적립하기"),
            LlmUiComponentDto(x = 274, y = 847, title = "카드결제"),
            LlmUiComponentDto(x = 458, y = 415, title = "취소 8900원"),
            LlmUiComponentDto(x = 107, y = 921, title = "다음으로"),
            LlmUiComponentDto(x = 376, y = 534, title = "스프라이트 8500원")
        )

        //when: llm에게 질의
        val response = placeAgent.determineAction(uiList)

        //then: 올바른 액션 반환
        assertThat(response.size).isEqualTo(1)
        assertThat(response[0].title).isEqualTo("")
        assertThat(response[0].coordinate[0]).isEqualTo(-1)
        assertThat(response[0].coordinate[1]).isEqualTo(-1)
    }
}