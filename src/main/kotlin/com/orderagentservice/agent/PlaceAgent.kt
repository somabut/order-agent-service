package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentPlaceDto
import com.orderagentservice.agent.model.dto.AgentUiDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import com.orderagentservice.order.exception.LlmParseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PlaceAgent @Autowired constructor(
    private val llmManager: LlmManager,
    private val usageTracker: UsageTracker
) {
    fun determineAction(uiList: List<UiComponentDto>): List<AgentPlaceDto> {
        val prompt = getPrompt(uiList)
        val answer = llmManager.query(prompt)
        val json = answer.content
        usageTracker.totalUsage += answer.usage

        val response: List<AgentPlaceDto> = jsonMapper.readValue<List<AgentPlaceDto>>(json)
        return response
    }

    private fun getPrompt(uiList: List<UiComponentDto>): String {
        val prompt = """
            You are an expert at judging what a Dine In/Take Out UI is like in a given ui list.    
            The response should be in Json format.
            
            Here is ui list:${uiList}
            
            IMPORTANT NOTES:
            1. You need to find and return all the Dine In/Take Out UI.
            2. An example of dine in UI is to eat at a current restaurant such as '먹고가기', '매장식사', '매장', '홀', '테이블', '내부식사' etc.
            3. An example of take out UI means that you take it without eating at the current restaurant such as '포장', '테이크아웃', '픽업', '포장주문', '가져가기' etc.
            4. Make sure to return the 'coordinate' is on the ui list
            5. If UI means take out, write '포장' and if it means dine in, write '매장'
            6. If neither Dine-In/Take-Out UI can be found, leave 'title' as an empty string and set the 'coordinate' to [-1, -1].
            7. The response should be scored on what you judged on the input. This score is the accuracy of your response you think.
            8. The score is between 0 and 1 and it's marked as a float.
            9. Response logic:
               - If BOTH dine-in AND take-out UI are found: return both
               - If NEITHER can be found: return empty string
               - Success: [{"title": "매장", "coordinate": [x, y], "bbox": [min_x, min_y, max_x, max_y], "origin": ""}, {"title": "포장", "coordinate": [x, y], "bbox": [min_x, min_y, max_x, max_y], "origin": ""}] 
                 (See the example below for more detailed formatting)
               - Not found: [{"title": "", "coordinate": [-1, -1], "bbox": [-1, -1, -1, -1], "origin": ""}] (See the example below for more detailed formatting)
            10. Please add a field called 'origin' to each JSON object and put the original title value of the UI matched in the uiList in this field.
            
            'goNext' is always false, 'score' is the accuracy score, 'coordinate' is the UI coordinate, 'title' is a string that follows Rule 5.
            'bbox' and 'origin' must be from 'uiList'. These value must never be returned to empty values.
             
             One Example(
                ui_list: [
                    {"coordinate":[210, 364], "title":"결제하기", "bbox":[144, 354, 276, 374]},
                    {"coordinate":[67, 90], "title":"카페라떼 5400원-", "bbox":[0, 80, 147, 100]},
                    {"coordinate":[123, 87], "title":"먹고가기", "bbox":[51, 77, 195, 97]},
                    {"coordinate":[90, 456], "title":"카푸치노 6000원-", "bbox":[0, 446, 138, 466]},
                    {"coordinate":[120, 74], "title":"포장하기", "bbox":[36, 64, 204, 84]}
                ]):
            ```json
            [
                {
                    "goNext": "false",
                    "score": 1.0,
                    "coordinate": [123, 87],
                    "bbox":[51, 77, 195, 97],
                    "origin": "먹고가기",
                    "title": "매장"
                },
                {
                    "goNext": "false",
                    "score": 1.0,
                    "coordinate": [120, 74],
                    "bbox":[36, 64, 204, 84],
                    "origin": "포장하기",
                    "title": "포장"
                }
            ]
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate":[210, 364], "title":"매장", "bbox":[186, 354, 234, 374]},
                    {"coordinate":[67, 90], "title":"카페라떼 5400원-", "bbox":[0, 80, 147, 100]},
                    {"coordinate":[123, 87], "title":"포장", "bbox":[99, 77, 147, 97]},
                    {"coordinate":[90, 456], "title":"카푸치노 6000원-", "bbox":[0, 446, 138, 466]},
                    {"coordinate":[120, 74], "title":"처음으", "bbox":[36, 64, 132, 84]}
                ]):
            ```json
            [
                {
                    "goNext": "false",
                    "score": 1.0,
                    "coordinate": [210, 364],
                    "bbox":[186, 354, 234, 374],
                    "origin": "매장",
                    "title": "매장"
                },
                {
                    "goNext": "false",
                    "score": 1.0,
                    "coordinate": [123, 87],
                    "bbox":[99, 77, 147, 97],
                    "origin": "포장"
                    "title": "포장"
                }
            ]
            ```
            
            Another Example(
                ui_list: [
                    {"coordinate":[210, 364], "title":"결제하기", "bbox":[144, 354, 276, 374]},
                    {"coordinate":[67, 90], "title":"payco 적립", "bbox":[43, 80, 147, 100]},
                    {"coordinate":[123, 87], "title":"먹고가기", "bbox":[51, 77, 195, 97]},
                    {"coordinate":[90, 456], "title":"메뉴", "bbox":[66, 446, 114, 466]},
                    {"coordinate":[120, 74], "title":"번호조회", "bbox":[96, 64, 168, 84]}
                ]):
            ```json
            [
                {
                    "goNext": "false",
                    "score": 0.9,
                    "coordinate": [-1, -1],
                    "bbox": [-1, -1, -1, -1],
                    "origin": "",
                    "title": ""
                }
            ]
            
            CRITICAL OUTPUT INSTRUCTION:
            Your entire response MUST be a valid JSON array, raw JSON object. It MUST start with [ and end with ]
            Do NOT include any introductory text, explanations, or markdown formatting like \\\json ... \\\ around the JSON object.
            ```
        """.trimIndent()

        return prompt
    }
}