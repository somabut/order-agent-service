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
        val json = llmManager.queryGemini(prompt)
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

            Primary Goal: Your main goal is to find a specific menu item. Falling back to a category is a secondary option only when the specific menu cannot be found.
            
            Decision Logic Flow (Follow these steps in order):
            1. Prioritize Direct Menu Match (Flexible Matching):
                First, iterate through the uiList. 
                For each ui, check if the input.title (e.g., "크리스퍼 클래식") is the core starting text of the ui.title (e.g., "크리스퍼 클래식 5600원" or "크리스퍼 클래식 세트"). 
                You must ignore trailing text like prices, weights, or simple options for this check.
                    Example 1 (Single Item): If input.title is '크리스퍼 클래식' and a ui.title is '크리스퍼 클래식 5600원', this is a successful match.
                    Example 2 (Set Menu): If input.title is '크리스퍼 클래식 세트' and a ui.title is '크리스퍼 클래식 세트 8900원', this is a successful match.
                
                Action for Match:
                    If you find one or more matches, choose the one that most completely matches the input.title. For example, if the input is '치즈버거' and the list has '치즈버거' and '더블치즈버거', select '치즈버거'.
                    Set the 'title' in your response to the input.title, Set 'goNext' to false. Use the 'coordinate' of the matched UI element.
                    
            2. Fallback to Category Match:
                Condition: Execute this step ONLY IF no direct menu match was found in Step 1.
                Action:
                    Find a UI element from the uiList where the ui.title exactly matches the input.category.
                    If a category match is found: Set the 'title' in your response to category of input not title of ui list, Set 'goNext to true, Use the 'coordinate' of the matched category UI element.
            
            'goNext' is whether to go to the next page, 'score' is the accuracy score, 'coordinate' is the UI coordinate you need to click to go to the next page and 'title' is the UI title that you click.
            
            SCORE NOTES:
            1. Perfect match (Score: 1.0)
                Situation: When the title of input matches exactly the title of the UI in the uiList.
                Judgment: the most ideal situation where no further exploration is needed.
            
            2. If the categories match perfectly (Score: 1.0)
                Situation: Title is not on the ui list, but when the category that came with input matches exactly the Title on the UI on the uiList.
                Judgment: The next step (move category) to find the menu is clear.
                
            3. When semantic/contextual categories match (Score: 0.8 to 0.9)
                Situation: neither input's title nor category is on the ui list, but when AI finds a top category on the ui list that is very similar in meaning to input's category or includes it.
                Judgment: No direct information, but we used AI's knowledge to infer the most likely next steps.
                
            4. Uncertain but most likely choice made (Score: 0.5 to 0.7)
                Situation: When there is no matching menu or clear category at all. But when ui list has a general UI to continue navigating, such as "모든 메뉴 보기", "다음", "더보기".
                Judgment: I can't find the answer on the current page, but I can suggest a way to continue my quest.
                
            5. If no action can be proposed (Score: 0.0 to 0.4)
                Situation: When matching menus, categories, and even general navigation UI like "다음" are not on ui list.
                Judgment: There is no valid click action that AI can do in the current state.

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
                "score": 1.0,
                "coordinate": [210, 364],
                "title": "아메리카노"
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
                "score": 1.0,
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
                    {"coordinate": [68, 12], "contents": ["에이드류"]},
                    {"coordinate": [90, 456], "contents": ["키위&망고 블렌더 7000원-"]},
                    {"coordinate": [120, 74], "contents": ["패션후르트&파인애플 스무디 7000원-"]}
                ]):
            ```json
            {
                "goNext": "true",
                "score": 0.9,
                "coordinate": [68, 12],
                "title": "에이드"
            }
            ```
            
            Another Example(
                input: {"title": "몬스터 주니어 라지세트", "option": ["콜라", "사이다"], "category": "올데이킹&맥모닝"} 
                ui_list: [
                    {"coordinate": [210, 364], "contents": ["오믈렛킹모닝 5600-"]},
                    {"coordinate": [67, 90], "contents": ["추천메뉴"]},
                    {"coordinate": [79, 89], "contents": ["사이드"]},
                    {"coordinate": [68, 12], "contents": ["몬스터 주니어6800-"]},
                    {"coordinate": [90, 456], "contents": ["불맛 더블치즈 주니어6200-"]},
                    {"coordinate": [120, 74], "contents": ["처음으로"]}
                ]):
            ```json
            {
                "goNext": "false",
                "score": 0.8,
                "coordinate": [68, 12],
                "title": "몬스터 주니어 라지세트"
            }
            ```
        """.trimIndent()

        return prompt
    }
}