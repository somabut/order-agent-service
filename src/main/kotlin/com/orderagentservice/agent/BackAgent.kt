package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import com.orderagentservice.order.exception.LlmParseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BackAgent @Autowired constructor(
    private val llmManager: LlmManager
) {
    private val log = logger()

    fun determineAction(uiList: List<UiComponentDto>): AgentBackDto {
        val prompt = getPrompt(uiList)
        val json = llmManager.query(prompt)
        try {
            val response: AgentBackDto = jsonMapper.readValue<AgentBackDto>(json)
            return response
        } catch (e: Exception) {
            throw LlmParseException()
        }
    }

    private fun getPrompt(uiList: List<UiComponentDto>): String {
        val prompt = """
            You are a professional at kiosks who complete shopping baskets.
            All menu selections are complete. Find the appropriate UI element from the given UI list to complete adding items to the cart.
            The response should be in Json format.
            
            Here is ui list:${uiList}
            
            Decision Algorithm:
            The AI must select only one UI according to the following algorithm.

            1. UI element detection
            Among all the items in the uiList provided, look for UI elements that semantically match one of the following words.
            Inclusion words: "담기", "카트담기", "완료", "다음", "확인", "선택"

            2. Select the final UI
            Based on the detected UI elements, select the final UI according to the following rules; the rules are applied in order from the top.
            
                Rule A: If there is a UI that matches the meaning of "담기" or "카트 담기" or "장바구니 담기"
                Condition: Is there a UI element in the list that is meaningfully consistent with "담기" or "카트 담기" or "장바구니 담기"?
                Action: Select one of them.

                Rule B: If there is a UI that matches meaningfully with "완료", "다음", "확인", and "선택"
                Condition: Does it not fall under Rule A, and does the list have UI elements that meaningfully match one of "완료", "다음", "확인", and "선택"?
                Action: Select one of them.
            
            'score' is the accuracy score, 'coordinate' and 'tittle' must be on the ui list.
            
            One Example(
                ui list: [
                    {"coordinate": [210, 364], "title": "샷 추가5000원-"},
                    {"coordinate": [67, 90], "title": "망고 추가 5400원-"},
                    {"coordinate": [123, 87], "title": "스몰 5000원-"},
                    {"coordinate": [90, 456], "title": "카트 담기"},
                    {"coordinate": [90, 456], "title": "취소"}
                    {"coordinate": [120, 74], "title": "라지 5000원-"}
                ]):
            ```json
            {
                "score": 1.0,
                "coordinate": [90, 456],
                "title": "카트 담기"
            }
            ```
            
            Another Example(
                ui list: [
                    {"coordinate": [210, 364], "title": "면 추가 5000원-"},
                    {"coordinate": [67, 90], "title": "밥 추가 5400원-"},
                    {"coordinate": [123, 87], "title": "선택완료"},
                    {"coordinate": [123, 87], "title": "결제하기"},
                    {"coordinate": [788, 43], "title": "취소"},
                    {"coordinate": [129, 74], "title": "차슈 추가 5000원-"}
                ]):
            ```json
            {
                "score": 1.0,
                "coordinate": [123, 87],
                "title": "선택완료"
            }
            ```
            
            Another Example(
                ui list: [
                    {"coordinate": [210, 364], "title": "감자튀김 5000원-"},
                    {"coordinate": [67, 90], "title": "제로콜라 5400원-"},
                    {"coordinate": [123, 87], "title": "사이다 5000원-"},
                    {"coordinate": [120, 74], "title": "다음으로"},
                    {"coordinate": [21, 78], "title": "취소"}
                ]):
            ```json
            {
                "score": 0.9,
                "coordinate": [120, 74],
                "title": "다음으로"
            }
            ```
            
            Another Example(
                ui list: [
                    {"coordinate": [210, 364], "title": "감자튀김 5000원-"},
                    {"coordinate": [67, 90], "title": "제로콜라 5400원-"},
                    {"coordinate": [123, 87], "title": "주문하기"},
                    {"coordinate": [21, 78], "title": "확인"}
                ]):
            ```json
            {
                "score": 0.8,
                "coordinate": [21, 78],
                "title": "확인"
            }
            ```
            
            CRITICAL OUTPUT INSTRUCTION:
            Your final response MUST be a single, raw JSON object. Do NOT include any introductory text, explanations, or markdown formatting like \\\json ... \\\ around the JSON object.
        """.trimIndent()

        return prompt
    }
}