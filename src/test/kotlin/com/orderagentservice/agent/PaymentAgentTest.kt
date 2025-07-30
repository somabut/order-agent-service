package com.orderagentservice.agent

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PaymentAgentTest @Autowired constructor(
    private val paymentAgent: PaymentAgent
) {
    @Test
    fun `카드를 넣기 전까지 페이지를 이동한다`() {
        //when: ui list가 주어진다
        val uiList = mutableListOf(
            LlmUiComponentDto(x = 150, y = 200, title = "장바구니 보기"),
            LlmUiComponentDto(x = 290, y = 200, title = "취소"),
            LlmUiComponentDto(x = 290, y = 400, title = "변경하기"),
            LlmUiComponentDto(x = 220, y = 500, title = "이전"),
            LlmUiComponentDto(x = 220, y = 600, title = "돌아가기")
        )

        uiList.add(LlmUiComponentDto(x = 220, y = 200, title = "완료"))

        //when-1: llm에게 질의 한다
        val response1 = paymentAgent.determineAction(uiList)
        println(response1)

        //then-1: 올바른 액션을 반환한다
        assertThat(response1.title).isEqualTo("완료")
        assertThat(response1.goNext).isEqualTo(true)
        uiList.removeLast()
        Thread.sleep(1000)

        uiList.add(LlmUiComponentDto(x = 220, y = 200, title = "결제하기"))
        uiList.add(LlmUiComponentDto(x = 330, y = 300, title = "다음"))

        //when-2: llm에게 질의 한다
        val response2 = paymentAgent.determineAction(uiList)
        println(response2)

        //then-2: 올바른 액션을 반환한다
        assertThat(response2.title).isEqualTo("다음")
        assertThat(response2.goNext).isEqualTo(true)
        uiList.removeLast()
        uiList.removeLast()
        Thread.sleep(1000)

        uiList.add(LlmUiComponentDto(x = 220, y = 200, title = "카드를 삽입해주세요"))

        //when-3: llm에게 질의 한다
        val response3 = paymentAgent.determineAction(uiList)
        println(response3)

        //then-3: 올바른 액션을 반환한다
        assertThat(response3.title).isEqualTo("카드를 삽입해주세요")
        assertThat(response3.goNext).isEqualTo(false)
        uiList.removeLast()
        Thread.sleep(1000)

        uiList.add(LlmUiComponentDto(x = 220, y = 200, title = "신용/체크카드"))
        uiList.add(LlmUiComponentDto(x = 330, y = 300, title = "결제"))

        //when-4: llm에게 질의 한다
        val response4 = paymentAgent.determineAction(uiList)
        println(response4)

        //then-4: 올바른 액션을 반환한다
        assertThat(response4.title).isEqualTo("신용/체크카드")
        assertThat(response4.goNext).isEqualTo(true)
        uiList.removeLast()
        uiList.removeLast()
        Thread.sleep(1000)

        uiList.add(LlmUiComponentDto(x = 330, y = 300, title = "결제"))

        //when-4: llm에게 질의 한다
        val response5 = paymentAgent.determineAction(uiList)
        println(response5)

        //then-4: 올바른 액션을 반환한다
        assertThat(response5.title).isEqualTo("결제")
        assertThat(response5.goNext).isEqualTo(true)
        uiList.removeLast()
        Thread.sleep(1000)
    }
}