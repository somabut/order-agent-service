package com.orderagentservice.agent

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StepAgentTest @Autowired constructor(
    private val stepAgent: StepAgent
){
    @Test
    fun `주문을 질의하면 스텝별로 나눈다`() {
        //given: 사용자의 주문
        val input = "시오라멘 면추가 차슈추가 2개, 크리스퍼 클래식 펩시제로추가 코울슬로추가 맥너겟추가 3개, 크리스피 윙 2조각, 포장 카드 결제해줘."
        val answer = listOf("시오라멘 면추가 차슈추가 2개", "크리스퍼 클래식 펩시제로추가 코울슬로추가 맥너겟추가 3개", "크리스피 윙 2조각", "포장", "카드 결제해줘")

        //when: 주문을 액션스탭별로 나눈다
        val steps = stepAgent.determineAction(input)

        //then: 스탭별로 나누어진다
        for(i in 0..4) {
            println(steps.steps[i])
            assertThat(steps.steps[i]).isEqualTo(answer[i])
        }
    }
}