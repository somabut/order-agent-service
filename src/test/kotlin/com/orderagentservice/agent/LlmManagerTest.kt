package com.orderagentservice.agent

import com.orderagentservice.agent.util.LlmManager
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LlmManagerTest @Autowired constructor(
    private val llmManager: LlmManager
){
    @Test
    fun `gemini에게 질의하여 응답을 얻는다`() {
        //given: 입력과 프롬프트
        val input = "안녕 응답이 잘 갔으면 정확하게 'moodTRBL'이라고만 답해"

        //when: llm에게 질의를 한다
        val response = llmManager.query(input)

        //then:
        assertThat(response).isEqualTo("moodTRBL")
    }

    @Test
    fun `gpt에게 질의하여 응답을 얻는다`() {
        //given: 입력과 프롬프트
        val input = "안녕 요청이 잘 갔으면 응답을 정확하게 'moodTRBL'로 반환해"

        //when: llm에게 질의를 한다
        val response = llmManager.queryGpt(input)

        //then:
        assertThat(response).isEqualTo("moodTRBL")
    }

    @Test
    fun `claud에게 질의하여 응답을 얻는다`() {
        //given: 입력과 프롬프트
        val input = "안녕 요청이 잘 갔으면 응답을 정확하게 'moodTRBL'로 반환해"

        //when: llm에게 질의를 한다
        val response = llmManager.queryClaud(input)

        //then:
        assertThat(response).isEqualTo("moodTRBL")
    }
}