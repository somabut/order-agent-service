package com.orderagentservice.agent

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
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

    fun determineBack(uiList: List<LlmUiComponentDto>): AgentBackDto {
        val prompt = getPrompt(uiList)
        val json = llmManager.queryGemini(prompt)
        try {
            val response: AgentBackDto = jsonMapper.readValue<AgentBackDto>(json)
            return response
        } catch (e: Exception) {
            throw LlmParseException()
        }
    }

    private fun getPrompt(uiList: List<LlmUiComponentDto>): String {
        val prompt = """
            You are a professional at kiosks who complete shopping baskets.
            All menu selections are complete. Find the appropriate UI element from the given UI list to complete adding items to the cart.
            The response should be in Json format.
            
            Here is ui list:${uiList}
            
            Decision Algorithm:
            The AI must select only one UI according to the following algorithm.

            Step 1: Classifying UI Elements
                First, classify all items in the given uiList into four grades below.
                * Grade 1 (most obvious choice): direct addition to ordering, etc. (e.g. "주문하기" "주문", "결제")
                * Grade 2 (obvious choice): direct addition to your shopping cart, such as putting, adding etc. (e.g. "담기", "카트담기")
                * Grade 3 (Progress to the next step): the act of completing the current step, such as completing, next, and continuing (e.g., "완료", "다음", "확인")
                * Grade 5 (inadequate selection): '취소', '뒤로가기', etc

            Step 2: Select the final UI
                Based on the classified results, select the final UI according to the rules below. The rules apply only once in order from the top.
                Rule A: When multiple grades are mixed (most important)
                * Condition: Are there more than 2 UI of different grades in the list: 1, 2, or 3?
                * Action: If so, unconditionally select the UI with the largest rating number. (Choice priority: Grade 3 > Grade 2 > Grade 1 )
                * Score: 0.8
                * Absolute example:
                * ["주문" (Grade 1), "카드 담기" (Grade 2)] If there is, you must select "카트 담기".
                * ["카트담기" (Grade 2) and "확인" (Grade 3)] If there is, you must select "확인".
                * ["주문" (Grade 1) and "확인" (Grade 3)] If there is, you must select "확인".
                * ["결제하기" (Grade 1) and "선택완료" (Grade 3)] If there is, you must select "선택완료".
                    
                Rule B: When there is only Grade 1
                * Condition: Not in Rule A, is there only a Grade 1 UI in the list?
                * Action: Select one of the Grade 1 UIs.
                * Score: 1.0
                
                Rule C: When there is only Grade 2
                * Condition: Does it not fall under rules A, B, only grade 2 UI in the list?
                * Action: Select one of the Grade 2 UI.
                * Score: 0.9
                
                Rule D: When there is only grade 3
                * Condition: Not for all of the above rules, is there only a Grade 3 UI in the list?
                * Action: Select one of the Grade 3 UIs.
                * Score: 0.7 to 0.8
                
                Rule E: Unselectable
                * Condition: Does it not fall under any of the above rules?
                * Action: Give up your choice.
                * Score: 0.0  
            
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
                    {"coordinate": [120, 74], "title": "다음으로"}
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