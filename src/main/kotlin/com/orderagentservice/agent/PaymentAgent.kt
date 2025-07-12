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
            You are an expert at judging the UI you need to physically insert a card in a kiosk order.
            The ui list has a list of currently clickable ui.
            The response should be in Json format.
            
            Here is ui list:${uiList}
            
            IMPORTANT NOTES:
            1. All the items have been added to the cart, and only the payment is left.
            2. You just need to let me know what UI to interact with until you put the card in.
            3. You may have to go through multiple pages before you put the card in, 
            so you can look at the given list and let me know which UIs you need to interact with to get to the page where you put the card in.
            4. Please let me know the 'coordinate' in the response as the UI I need to click to go to the page with the ui corresponding to the input. 
            Please add coordinate information for 'coordinate'.
            5. Make sure to return the 'coordinate' and 'title' in the response to those in the ui list.
            6. If it means to insert a card, put false in the 'goNext' in the response. If not, put true to 'goNext'.
            7. The response should be scored on what you judged on the input. This score is the accuracy of your response you think.
            8. The score is between 0 and 1 and it's marked as a float.
            9. In order to get to the page with the sentence to ask for a card, you have to choose the payment method as a '카드' or '체크카드' or '카드결제' in the middle.
            10. The following is an example of a sentence that means to put a card in. [ex) '카드를 넣어주세요', '카드를 삽입해주세요', '카드 넣어'... etc]
            
            'goNext' is whether to go to the next page, 'score' is the accuracy score, 
            'coordinate' is the UI coordinate you need to click to go to the next page and 'title' is the UI title that you click.
            
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
                "goNext": "false",
                "score": 0.9,
                "coordinate": [67, 90],
                "title": "카드를 넣어주세요"
            }
            ```
            
        """.trimIndent()

        return prompt
    }
}