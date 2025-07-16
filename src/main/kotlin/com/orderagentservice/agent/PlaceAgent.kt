package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PlaceAgent @Autowired constructor(
    private val llmManager: LlmManager
) {
    fun determineAction(uiList: List<LlmUiComponentDto>): List<AgentActionDto> {
        val prompt = getPrompt(uiList)
        val json = llmManager.queryGemini(prompt)
        val response: List<AgentActionDto> = jsonMapper.readValue<List<AgentActionDto>>(json)
        return response
    }

    private fun getPrompt(uiList: List<LlmUiComponentDto>): String {
        val prompt = """
            You are an expert at judging what a Dine In/Take Out UI is like in a given ui list.    
            The response should be in Json format.
            
            Here is ui list:${uiList}
            
            IMPORTANT NOTES:
            1. You need to find and return all the Dine In/Take Out UI.
            2. An example of dine in UI is to eat at a current restaurant such as '먹고가기', '매장' etc.
            3. An example of take out UI means that you take it without eating at the current restaurant such as '포장' etc
            4. Make sure to return the 'coordinate' is on the ui list
            5. If UI means take out, write '포장' and if it means dine in, write '매장'
            6. If the Dine In/Take Out UI cannot be found, leave 'title' as an empty string and set the 'coordinate' to [-1, -1].
            7. The response should be scored on what you judged on the input. This score is the accuracy of your response you think.
            8. The score is between 0 and 1 and it's marked as a float.
            
            'goNext' is always false, 'score' is the accuracy score, 'coordinate' is the UI coordinate, 'title' is a string that follows Rule 5.
            
            The criteria for the 'score' are as follows. 
                "I'm sure I got it right" → 1.0
                "Meaning or context is correct, but not 100% sure" → 0.7~0.9
                "Looks similar but uncertain" → 0.5~0.6
                "Almost not sure" → 0~0.4
             
             One Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "결제하기"},
                    {"coordinate": [67, 90], "title": "카페라떼 5400원-"},
                    {"coordinate": [123, 87], "title": "먹고가기"},
                    {"coordinate": [90, 456], "title": "카푸치노 6000원-"},
                    {"coordinate": [120, 74], "title": "포장하기"}
                ]):
            ```json
            [
                {
                    "goNext": "false",
                    "score": 0.8,
                    "coordinate": [123, 87],
                    "title": "매장"
                },
                {
                    "goNext": "false",
                    "score": 0.8,
                    "coordinate": [120, 74],
                    "title": "포장"
                }
            ]
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate": [210, 364], "title": "결제하기"},
                    {"coordinate": [67, 90], "title": "payco 적립"},
                    {"coordinate": [123, 87], "title": "먹고가기"},
                    {"coordinate": [90, 456], "title": "메뉴"},
                    {"coordinate": [120, 74], "title": "번호조회"}
                ]):
            ```json
            [
                {
                    "goNext": "false",
                    "score": 0.8,
                    "coordinate": [-1, -1],
                    "title": ""
                }
            ]
            ```
        """.trimIndent()

        return prompt
    }
}