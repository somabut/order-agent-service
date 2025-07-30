package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentAgent @Autowired constructor(
    private val llmManager: LlmManager
) {
    fun determineAction(uiList: List<LlmUiComponentDto>): AgentActionDto {
        val prompt = getPrompt(uiList)
        val json = llmManager.queryGemini(prompt)
        val response: AgentActionDto = jsonMapper.readValue<AgentActionDto>(json)
        return response
    }

    private fun getPrompt(uiList: List<LlmUiComponentDto>): String {
        val prompt = """            
            You are a kiosk payment expert. Items are in cart, payment remains. Identify UI to click, leading to card insertion page.
            The ui list has a list of currently clickable ui.
            The response should be in Json format.
            
            Here is ui list:${uiList}
            
            IMPORTANT NOTES:
            Please let me know the 'coordinate' in the response as the UI I need to click to go to the page with the ui corresponding to the input. Please add coordinate information for 'coordinate'.
            To select the correct UI, you must follow this strict priority order:
            
                Priority 1 (Highest): Direct Card Payment
                    First, search for UI elements containing the most specific keywords: '신용카드', '체크카드', '카드결제'.
                    If you find any of these, you must select one of them and ignore all other options below.

                Priority 2 (Middle): General Progression
                    Only if no Priority 1 keywords are found, search for UI elements with general progression keywords like '다음', '확인', '완료'.
                    If you find any of these, select one and ignore the Priority 3 options.

                Priority 3 (Lowest): Generic Payment
                    Only if no Priority 1 or 2 keywords are found, search for UI elements with generic payment keywords like '결제하기', '결제', '결제수단 선택'.
                    Crucial Rule: Always choose the option with the highest priority. For example, if '확인'(Priority 2) and '결제하기'(Priority 3) are both visible, you must choose '확인'.

            The final goal is to reach a page that contains phrases like '카드를 넣어주세요' or '카드를 삽입해주세요'.
            
            Respond in JSON: {'coordinate': [x, y], 'title': 'UI Title', 'goNext': bool, 'score': float}. 
            'goNext' is always true. 'coordinate' and 'title' must be from 'uiList'.
            Assign a score (0.0-1.0) for response accuracy.
            
            One Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "결제하기"},
                    {"coordinate": [67, 90], "title": "카페라떼 5400원-"},
                    {"coordinate": [123, 87], "title": "카라멜 마끼야또 5000원-"},
                    {"coordinate": [90, 456], "title": "카푸치노 6000원-"},
                    {"coordinate": [120, 74], "title": "바닐라 라떼 5000원-"}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.8,
                "coordinate": [210, 364],
                "title": "결제하기"
            }
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "이전"},
                    {"coordinate": [67, 90], "title": "전체 취소"},
                    {"coordinate": [123, 87], "title": "주문 금액"},
                    {"coordinate": [90, 456], "title": "확인"},
                    {"coordinate": [120, 74], "title": "결제 금액"}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.8,
                "coordinate": [90, 456],
                "title": "확인"
            }
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "payco 적립"},
                    {"coordinate": [67, 90], "title": "번호조회"},
                    {"coordinate": [123, 87], "title": "쿠폰 사용"},
                    {"coordinate": [90, 456], "title": "메뉴"},
                    {"coordinate": [120, 74], "title": "완료"}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.9,
                "coordinate": [120, 74],
                "title": "완료"
            }
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "쿠폰 결제"},
                    {"coordinate": [67, 90], "title": "카드 결제"},
                    {"coordinate": [123, 87], "title": "payco"},
                    {"coordinate": [90, 456], "title": "이전"},
                    {"coordinate": [120, 74], "title": "취소"}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 1.0,
                "coordinate": [67, 90],
                "title": "카드 결제"
            }
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "현재 금액"},
                    {"coordinate": [67, 90], "title": "카드를 넣어주세요"},
                    {"coordinate": [123, 87], "title": "할인 금액"},
                    {"coordinate": [90, 456], "title": "결제 금액"},
                    {"coordinate": [120, 74], "title": "취소"}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.9,
                "coordinate": [67, 90],
                "title": "카드를 넣어주세요"
            }
            ```
            
        """.trimIndent()

        return prompt
    }
}