package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.agent.model.dto.AgentBackDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
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
        val response: AgentBackDto = jsonMapper.readValue<AgentBackDto>(json)
        return response
    }

    private fun getPrompt(uiList: List<LlmUiComponentDto>): String {
        val prompt = """
            You are a professional at kiosks who complete shopping baskets.
            All menu selections are complete. Find the appropriate UI element from the given UI list to complete adding items to the cart.
            The response should be in Json format.
            
            Here is ui list:${uiList}
            
            IMPORTANT NOTES:
            1. To complete what you put in your cart, you need to interact with the following UI: '담기', '완료', '계속', '추기', and '다음'
            2. The UI to complete what you put in your shopping cart means moving on or completing the current action.
            3. The response should be scored on what you judged on the input. This score is the accuracy of your response you think.
            4. The score is between 0 and 1 and it's marked as a float. 
            5. Make sure to return the 'coordinate' and 'title' in the response to those in the ui list.
            
            SCORE NOTES:
            1. UI which clearly means 'to put in the shopping cart (Score: 1.0)
                Situation: When there is a UI that directly indicates the action of adding the currently selected menu (and options) to your shopping cart. This action is 'Exit screen after selection'.
                Judgment: The most accurate command that matches 100% of the goal of 'completed and returned' that the user wants.
            
            2. Comprehensive UI meaning 'completed/next' (Score: 0.9)
                Situation: There's no direct expression for '담기', but when there's a UI that means "getting on" with the current stage.
                Judgment: '완료' or '다음' means finishing your current option selection and moving on to the next process (mainly checking your shopping cart), so it's almost in line with your intentions.
                
            3. UI to close the current window through '확인' (Score: 0.8)
                Situation: When the above keywords are missing, only the '확인' button. This confirmation is likely to serve to confirm the option selection and close the window.
                Judgment: Confirmation can also mean other actions (e.g., simple notification confirmation), which makes it somewhat ambiguous rather than complete or contain it. 
                          However, it is the most likely 'return after completion' means of choice.
            
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
                "score": 0.8,
                "coordinate": [90, 456],
                "title": "카트 담기"
            }
            ```
            
            Another Example(
                ui list: [
                    {"coordinate": [210, 364], "title": "면 추가 5000원-"},
                    {"coordinate": [67, 90], "title": "밥 추가 5400원-"},
                    {"coordinate": [123, 87], "title": "완료"},
                    {"coordinate": [129, 74], "title": "차슈 추가 5000원-"}
                ]):
            ```json
            {
                "score": 0.7,
                "coordinate": [123, 87],
                "title": "완료"
            }
            ```
            
            Another Example(
                ui list: [
                    {"coordinate": [210, 364], "title": "감자튀김 5000원-"},
                    {"coordinate": [67, 90], "title": "제로콜라 5400원-"},
                    {"coordinate": [123, 87], "title": "사이다 5000원-"},
                    {"coordinate": [120, 74], "title": "다음으로"}
                ]):
            ```json
            {
                "score": 0.8,
                "coordinate": [120, 74],
                "title": "다음으로"
            }
            ```
            
        """.trimIndent()

        return prompt
    }
}