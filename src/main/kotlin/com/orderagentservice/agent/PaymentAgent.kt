package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentUiDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentAgent @Autowired constructor(
    private val llmManager: LlmManager
) {
    fun determineAction(uiList: List<UiComponentDto>): AgentActionDto {
        val prompt = getPrompt(uiList)
        val json = llmManager.query(prompt)
        val response: AgentActionDto = jsonMapper.readValue<AgentActionDto>(json)
        return response
    }

    private fun getPrompt(uiList: List<UiComponentDto>): String {
        val prompt = """            
            You are a kiosk payment expert. Items are in cart, payment remains. Identify UI to click, leading to card insertion page.
            The ui list has a list of currently clickable ui.
            The response should be in Json format.
            
            Here is ui list:${uiList}
            
            IMPORTANT NOTES:
            Please let me know the 'coordinate' in the response as the UI I need to click to go to the page with the ui corresponding to the input. Please add coordinate information for 'coordinate'.
            To select the correct UI, you must follow this strict priority order:
            
                Priority 1 (Very Highest): Insert Card
                    First, look for a keyword that includes the meaning of inserting a card: '카드를 넣어주세요', '카드를 삽입해주세요', '입구에 꽃아주세요'.
                    If you find any of these, you must select one of them and ignore all other options below.
                    
                Priority 2 (Highest): General Progression
                    Only if no Priority 1 keywords are found, search for UI elements with general progression keywords like '다음', '확인', '완료', '선택완료'.
                    If you find any of these, syou must select one of them and ignore all other options below.
                
                Priority 3 (Middle): Direct Card Payment
                    First, find a keyword that has a meaning related to the card: '신용카드', '체크카드', '카드결제'
                    If you find any of these, you must select one of them and ignore all other options below.

                Priority 4 (Lowest): Generic Payment
                    Only if no Priority 2 or 3 keywords are found, search for UI elements with generic payment keywords like '결제하기', '결제'
                    If you find any of these, you must select one of them and ignore all other options below.

            The final goal is to reach a page that contains phrases like '카드를 넣어주세요' or '카드를 삽입해주세요' or '입구에 꽂아주세요'
            
            Respond in JSON: {'coordinate': [x, y], 'title': 'UI Title', 'goNext': bool, 'score': float}. 
            if you get a sentence that means to put a card in like '카드를 넣어주세요' or '카드를 삽입해주세요', write response of 'isNext' false, or true. 
            'coordinate', 'bbox' and 'title' must be from 'uiList'.
            Assign a score (0.0-1.0) for response accuracy.
            
            One Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "결제하기", "bbox": [190, 354, 230, 374]},
                    {"coordinate": [67, 90], "title": "카페라떼 5400원-", "bbox": [47, 80, 87, 100]},
                    {"coordinate": [123, 87], "title": "카라멜 마끼야또 5000원-", "bbox": [103, 77, 143, 97]},
                    {"coordinate": [90, 456], "title": "카푸치노 6000원-", "bbox": [70, 446, 110, 466]},
                    {"coordinate": [120, 74], "title": "바닐라 라떼 5000원-", "bbox": [100, 64, 140, 84]}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.8,
                "coordinate": [210, 364],
                "bbox": [190, 354, 230, 374],
                "title": "결제하기"
            }
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "이전", "bbox": [190, 354, 230, 374]},
                    {"coordinate": [67, 90], "title": "전체 취소", "bbox": [47, 80, 87, 100]},
                    {"coordinate": [123, 87], "title": "주문 금액", "bbox": [103, 77, 143, 97]},
                    {"coordinate": [90, 456], "title": "확인", "bbox": [70, 446, 110, 466]},
                    {"coordinate": [120, 74], "title": "결제 금액", "bbox": [100, 64, 140, 84]}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.8,
                "coordinate": [90, 456],
                "bbox": [70, 446, 110, 466],
                "title": "확인"
            }
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate":[210, 364], "title":"payco 적립", "bbox":[186, 354, 234, 374]},
                    {"coordinate":[67, 90], "title":"번호조회", "bbox":[43, 80, 91, 100]},
                    {"coordinate":[123, 87], "title":"쿠폰 사용", "bbox":[99, 77, 147, 97]},
                    {"coordinate":[90, 456], "title":"메뉴", "bbox":[66, 446, 114, 466]},
                    {"coordinate":[120, 74], "title":"완료", "bbox":[96, 64, 144, 84]}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.9,
                "coordinate": [120, 74],
                "bbox":[96, 64, 144, 84],
                "title": "완료"
            }
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate":[210, 364], "title":"쿠폰 결제", "bbox":[186, 354, 234, 374]},
                    {"coordinate":[67, 90], "title":"카드 결제", "bbox":[43, 80, 91, 100]},
                    {"coordinate":[123, 87], "title":"payco", "bbox":[99, 77, 147, 97]},
                    {"coordinate":[90, 456], "title":"이전", "bbox":[66, 446, 114, 466]},
                    {"coordinate":[120, 74], "title":"취소", "bbox":[96, 64, 144, 84]}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 1.0,
                "coordinate": [67, 90],
                "bbox":[43, 80, 91, 100],
                "title": "카드 결제"
            }
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate":[210, 364], "title":"현재 금액", "bbox":[186,354,234,374]},
                    {"coordinate":[67, 90], "title":"카드를 넣어주세요", "bbox":[7,80,127,100]},
                    {"coordinate":[123, 87], "title":"할인 금액", "bbox":[99,77,147,97]},
                    {"coordinate":[90, 456], "title":"결제 금액", "bbox":[66,446,114,466]},
                    {"coordinate":[120, 74], "title":"취소", "bbox":[96,64,144,84]}
                ]):
            ```json
            {
                "goNext": "false",
                "score": 0.9,
                "coordinate": [67, 90],
                "bbox":[7, 80, 127, 100],
                "title": "카드를 넣어주세요"
            }
            ```
            
            CRITICAL OUTPUT INSTRUCTION:
            Your final response MUST be a single, raw JSON object. Do NOT include any introductory text, explanations, or markdown formatting like \\\json ... \\\ around the JSON object.
        """.trimIndent()

        return prompt
    }
}