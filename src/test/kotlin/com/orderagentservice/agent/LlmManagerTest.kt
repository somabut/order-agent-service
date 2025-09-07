package com.orderagentservice.agent

import com.orderagentservice.agent.util.LlmManager
import kotlinx.coroutines.*
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

    @Test
    fun `코루틴으로 빠르게 질의한다`() {
        runBlocking {
            val jobs = (1..5).map { i ->
                async(Dispatchers.IO) { // IO 디스패처로 멀티스레드 실행
                    val result = llmManager.queryGpt("다니엘 시저랑 프랭크 오션중 누가 더 RnB를 잘하는지 한마디로 대답해.")
                    println("응답 $i: $result")
                    result
                }
            }

            // 모든 결과 대기
            val results = jobs.awaitAll()
            println("모든 요청 완료: ${results.size}개")
        }
    }
}