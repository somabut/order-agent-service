package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import org.assertj.core.api.Assertions
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
            LlmUiComponentDto(x = 145, y = 310, title = "프렌치 프라이 5600원"),
            LlmUiComponentDto(x = 212, y = 485, title = "너겟킹 7900원"),
            LlmUiComponentDto(x = 398, y = 605, title = "리얼 어니언링 8200원"),
            LlmUiComponentDto(x = 125, y = 732, title = "다음으로"),
            LlmUiComponentDto(x = 362, y = 298, title = "코카콜라 7500원"),
            LlmUiComponentDto(x = 274, y = 847, title = "코카콜라 제로 9500원"),
            LlmUiComponentDto(x = 458, y = 415, title = "취소"),
            LlmUiComponentDto(x = 107, y = 921, title = "미밋메이드 오렌지 8800원"),
            LlmUiComponentDto(x = 376, y = 534, title = "스프라이트 8500원")
        )

        //when: llm에게 질의
        val response = backAgent.determineBack(uiList)

        //then: 올바른 액션 반환
//        assertThat(response.score).isGreaterThan(0.8F)
        assertThat(response.title).isEqualTo("다음으로")
        assertThat(response.coordinate[0]).isEqualTo(125)
        assertThat(response.coordinate[1]).isEqualTo(732)
    }

    @Test
    fun `중복되는 완료 UI중에 적절한 것을 찾는다`() {
        //given: ui list
//        val uiList = listOf(
//            LlmUiComponentDto(x = 145, y = 310, title = "프렌치 프라이 5600원"),
//            LlmUiComponentDto(x = 212, y = 485, title = "너겟킹 7900원"),
//            LlmUiComponentDto(x = 398, y = 605, title = "리얼 어니언링 8200원"),
//            LlmUiComponentDto(x = 125, y = 732, title = "확인"),
//            LlmUiComponentDto(x = 67, y = 99, title = "주둔"),
//            LlmUiComponentDto(x = 362, y = 298, title = "코카콜라 7500원"),
//            LlmUiComponentDto(x = 274, y = 847, title = "코카콜라 제로 9500원"),
//            LlmUiComponentDto(x = 458, y = 415, title = "취소"),
//            LlmUiComponentDto(x = 107, y = 921, title = "미밋메이드 오렌지 8800원"),
//            LlmUiComponentDto(x = 376, y = 534, title = "스프라이트 8500원")
//        )
        val uiList = listOf(
            LlmUiComponentDto(x = 227, y = 30, title = "BURGER"),
            LlmUiComponentDto(x = 315, y = 29, title = "KING"),
            LlmUiComponentDto(x = 69, y = 106, title = "추천메뉴"),
            LlmUiComponentDto(x = 197, y = 106, title = "오리지널스움맥시멈"),
            LlmUiComponentDto(x = 324, y = 105, title = "프리미엄"),
            LlmUiComponentDto(x = 453, y = 106, title = "와퍼오주니어"),
            LlmUiComponentDto(x = 67, y = 172, title = "치킨&슈림프버기"),
            LlmUiComponentDto(x = 196, y = 173, title = "올데이킹&킹모님"),
            LlmUiComponentDto(x = 325, y = 173, title = "사이드"),
            LlmUiComponentDto(x = 452, y = 172, title = "음료&디저트"),
            LlmUiComponentDto(x = 113, y = 222, title = "원하시는구성을"),
            LlmUiComponentDto(x = 217, y = 222, title = "선택해주세요"),
            LlmUiComponentDto(x = 139, y = 306, title = "통모파와퍼"),
            LlmUiComponentDto(x = 196, y = 306, title = "라지세트"),
            LlmUiComponentDto(x = 136, y = 330, title = "통모찌와퍼"),
            LlmUiComponentDto(x = 213, y = 329, title = "프렌치프라이니"),
            LlmUiComponentDto(x = 295, y = 330, title = "코카콜라니"),
            LlmUiComponentDto(x = 141, y = 356, title = "10300원"),
            LlmUiComponentDto(x = 136, y = 448, title = "통모짜와퍼"),
            LlmUiComponentDto(x = 215, y = 449, title = "프렌치프리이R"),
            LlmUiComponentDto(x = 296, y = 448, title = "코카콜라R"),
            LlmUiComponentDto(x = 137, y = 476, title = "9600원"),
            LlmUiComponentDto(x = 140, y = 544, title = "통모파와피"),
            LlmUiComponentDto(x = 137, y = 595, title = "8300원"),
            LlmUiComponentDto(x = 24, y = 671, title = "주둔"),
            LlmUiComponentDto(x = 260, y = 692, title = "확인"),
            LlmUiComponentDto(x = 499, y = 724, title = "0개"),
            LlmUiComponentDto(x = 383, y = 752, title = "주문금액"),
            LlmUiComponentDto(x = 393, y = 789, title = "총결제금액"),
            LlmUiComponentDto(x = 142, y = 804, title = "담긴"),
            LlmUiComponentDto(x = 199, y = 804, title = "상품이없습니다"),
            LlmUiComponentDto(x = 434, y = 900, title = "주문하기")
        )


        //when: llm에게 질의
        val response = backAgent.determineBack(uiList)
        println(response)

        assertThat(response.title).isEqualTo("확인")
        assertThat(response.coordinate[0]).isEqualTo(125)
        assertThat(response.coordinate[1]).isEqualTo(732)
    }
}