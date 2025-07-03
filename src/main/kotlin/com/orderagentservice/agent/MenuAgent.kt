package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import com.orderagentservice.order.model.dto.MenuInfoDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuAgent @Autowired constructor(
    private val llmManager: LlmManager
) {
    private val log = logger()

    fun determineAction(menuDto: MenuInfoDto, uiList: List<LlmUiComponentDto>): AgentActionDto {
        val prompt = getPrompt(menuDto, uiList)
        val json = llmManager.queryGeminiModel(prompt)
        val response: AgentActionDto = jsonMapper.readValue<AgentActionDto>(json)
        return response
    }

    private fun getPrompt(menuDto: MenuInfoDto, uiList: List<LlmUiComponentDto>): String {
        val prompt = """
            You are an expert who looks at the UI list and determines if there is a UI for a given input.
            input is information that you need to click to order food.
            The ui list has a list of currently clickable ui.
            The response should be in Json format.

            Here is request input:${menuDto}
            Here is ui list:${uiList}

            IMPORTANT NOTES:
            1. It is determined that the string on the ui list must contain the string of input correctly to exist. [ex) If the input is '초코 케이크', if there is '딸기 케이크', '당근 케이크', '치즈 케이크', then exactly there is no '초코 케이크']
            2. If it doesn't exist on the ui list, please let me know which ui exists on the ui list to go to the page where the input exists. There are categories of these, such as 'category' and 'next'
            3. For UI information that you need to interact with to move on to the next page, please refer to the 'category' in the given input. 
            In other words, when you don't have a menu and need to go to a category, please make 'goNext' true
            4. A category is a broad word that can include a specific menu, not a specific menu name. [ex) '콜라', '주스', '사이다' is '음료' category. '감자튀김', '만두' is '사이드' category]
            5. Returns the category to 'title' only if the corresponding request input does not exist.
            6. The response should be scored on what you judged on the input. This score is the accuracy of your response you think.
            7. The score is between 0 and 1 and it's marked as a float.
            8. Please let me know the 'coordinate' in the response as the UI I need to click to go to the page with the ui corresponding to the input. Please add coordinate information for 'coordinate'.
            9. Make sure to return the 'coordinate' and 'title' in the response to those in the ui list.

            'goNext' is whether to go to the next page, 'score' is the accuracy score, 'coordinate' is the UI coordinate you need to click to go to the next page and 'title' is the UI title that you click.

            One Example(
                input: {"title": "아메리카노", "option": ["샷", "망고"], "category": "음료"} 
                ui_list: [
                    {"coordinate": [210, 364], "title": "아메라카노 5000원-"},
                    {"coordinate": [67, 90], "title": "카페라떼 5400원-"},
                    {"coordinate": [123, 87], "title": "카라멜 마끼야또 5000원-"},
                    {"coordinate": [90, 456], "title": "카푸치노 6000원-"},
                    {"coordinate": [120, 74], "title": "바닐라 라떼 5000원-"}
                ]):
            ```json
            {
                "goNext": "false",
                "score": 0.8,
                "coordinate": [210, 364],
                "title": "아메라카노 5000원-"
            }
            ```

            Another Example(
                input: {"title": "초콜릿 케이크", "option": [], "category": "디저트"} 
                ui_list: [
                    {"coordinate": [210, 364], "contents": ["아메라카노 5000원-"]},
                    {"coordinate": [67, 90], "contents": ["카페라떼 5400원-"]},
                    {"coordinate": [123, 87], "contents": ["카라멜 마끼야또 5000원-"]},
                    {"coordinate": [90, 456], "contents": ["카푸치노 6000원-"]},
                    {"coordinate": [120, 74], "contents": ["바닐라 라떼 5000원-"]},
                    {"coordinate": [120, 444], "contents": ["디저트"]}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.7,
                "coordinate": [120, 444],
                "title": "디저트"
            }
            ```

            Another Example(
                input: {"title": "딸기 에이드", "option": ["라지", "휘핑크림"], "category": "에이드"} 
                ui_list: [
                    {"coordinate": [210, 364], "contents": ["블루베리 스무디 7000원-"]},
                    {"coordinate": [67, 90], "contents": ["딸기 요거트 스무디 5400원-"]},
                    {"coordinate": [79, 89], "contents": ["결제"]},
                    {"coordinate": [68, 12], "contents": ["에이드"]},
                    {"coordinate": [90, 456], "contents": ["키위&망고 블렌더 7000원-"]},
                    {"coordinate": [120, 74], "contents": ["패션후르트&파인애플 스무디 7000원-"]}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.7,
                "coordinate": [68, 12],
                "title": "에이드"
            }
            ```
        """.trimIndent()

        return prompt
    }
}