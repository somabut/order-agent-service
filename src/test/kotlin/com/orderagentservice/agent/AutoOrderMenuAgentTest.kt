package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AutoOrderMenuAgentTest @Autowired constructor(
    private val menuAgent: MenuAgent
) {
    @Test
    fun `스텝에 대한 적절한 액션을 반환한다`() {
        //given: ui list와 액션 스텝
        val uiList = listOf(
            LlmUiComponentDto(x = 145, y = 310, title = "크리스피 클래식 5600원"),
            LlmUiComponentDto(x = 212, y = 485, title = "크리스퍼 팩 7900원"),
            LlmUiComponentDto(x = 398, y = 605, title = "통모짜와퍼 8200원"),
            LlmUiComponentDto(x = 125, y = 732, title = "BBQ 통모짜와퍼 8700원"),
            LlmUiComponentDto(x = 362, y = 298, title = "불끈버거 맥시멈 7500원"),
            LlmUiComponentDto(x = 274, y = 847, title = "불끈버거 맥시멈 더블 9500원"),
            LlmUiComponentDto(x = 458, y = 415, title = "몬스터 와퍼 8900원"),
            LlmUiComponentDto(x = 107, y = 921, title = "콰트로 치즈 와퍼 8800원"),
            LlmUiComponentDto(x = 376, y = 534, title = "통새우 와퍼 8500원"),
            LlmUiComponentDto(x = 198, y = 667, title = "통모짜와퍼주니어 6900원"),
            LlmUiComponentDto(x = 421, y = 373, title = "BBQ 통모짜와퍼 주니어 7100원"),
            LlmUiComponentDto(x = 139, y = 759, title = "불맛 더블치즈베이컨 버거 9200원"),
            LlmUiComponentDto(x = 266, y = 199, title = "베이컨치즈 와퍼 8300원"),
            LlmUiComponentDto(x = 308, y = 624, title = "불맛 치즈버거 6800원"),
            LlmUiComponentDto(x = 181, y = 890, title = "오믈렛 킹모닝 5400원"),
            LlmUiComponentDto(x = 331, y = 478, title = "몬스터 주니어 7400원"),
            LlmUiComponentDto(x = 412, y = 353, title = "더블비프 불고기 버거 7700원"),
            LlmUiComponentDto(x = 152, y = 947, title = "비프 불거기 버거 7100원"),
            LlmUiComponentDto(x = 298, y = 572, title = "핫 토마토 모짜볼 4800원"),
            LlmUiComponentDto(x = 269, y = 231, title = "바삭킹 4000원"),
            LlmUiComponentDto(x = 357, y = 698, title = "리얼 어니언링 3600원"),
            LlmUiComponentDto(x = 109, y = 329, title = "너겟킹 3900원"),
            LlmUiComponentDto(x = 489, y = 617, title = "바삭킹&조각 + 소스 5200원"),
            LlmUiComponentDto(x = 127, y = 496, title = "코코넛슈림프 5600원"),
            LlmUiComponentDto(x = 391, y = 822, title = "스위트 칠리 소스 900원"),
            LlmUiComponentDto(x = 318, y = 553, title = "크리미 모짜볼 4700원"),
            LlmUiComponentDto(x = 200, y = 342, title = "21치즈 스틱 3200원"),
            LlmUiComponentDto(x = 360, y = 710, title = "쉐이킹 프라이 3900원"),
            LlmUiComponentDto(x = 159, y = 268, title = "프렌치 프라이(L) 3400원"),
            LlmUiComponentDto(x = 226, y = 489, title = "코울슬로/콘셀러드 3100원"),
            LlmUiComponentDto(x = 401, y = 633, title = "소스 700원"),
            LlmUiComponentDto(x = 289, y = 914, title = "시즈닝 600원"),
            LlmUiComponentDto(x = 289, y = 234, title = "사이드"),
            LlmUiComponentDto(x = 115, y = 234, title = "음료"),
            LlmUiComponentDto(x = 28, y = 889, title = "결제하기")
        )
        val menuDto1 = MenuInfoDto(
            title = "BBQ 통모짜와퍼",
            options = listOf(),
            category = "햄버거"
        )
        val menuDto2 = MenuInfoDto(
            title = "제로콜라",
            options = listOf(),
            category = "음료"
        )

        //when: llm에게 질의 한다
        val response1 = menuAgent.determineAction(menuDto1, uiList)
        val response2 = menuAgent.determineAction(menuDto2, uiList)

        //then: 올바른 액션을 반환한다.
        assertThat(response1.title).isEqualTo("BBQ 통모짜와퍼")
        assertThat(response1.goNext).isEqualTo(false)
        assertThat(response1.coordinate[0]).isEqualTo(125)
        assertThat(response1.coordinate[1]).isEqualTo(732)

        assertThat(response2.title).isEqualTo("음료")
        assertThat(response2.goNext).isEqualTo(true)
        assertThat(response2.coordinate[0]).isEqualTo(115)
        assertThat(response2.coordinate[1]).isEqualTo(234)
    }
}