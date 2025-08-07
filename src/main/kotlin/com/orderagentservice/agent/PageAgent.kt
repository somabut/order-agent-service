package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.AgentPageDto
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.exception.LlmParseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PageAgent @Autowired constructor(
    private val llmManager: LlmManager
) {
    fun determineAction(metaList: List<String>, uiList: List<UiComponentDto>): AgentPageDto {
        val prompt = getPrompt(metaList, uiList)
        val json = llmManager.query(prompt)
        try {
            val response: AgentPageDto = jsonMapper.readValue<AgentPageDto>(json)
            return response
        } catch (e: Exception) {
            throw LlmParseException()
        }
    }

    private fun getPrompt(metaList: List<String>, uiList: List<UiComponentDto>): String {
        val prompt = """
            You are an expert who looks at a given ui text list and compares it to the current page information to find out if the ui text list is a component of the current page.
            The information about the page is on the page info list. In other words, the page info list is the information on the current page.
            The response should be in Json format.
            
            Here is ui text list:${metaList}
            Here is page info list:${uiList}
            
            IMPORTANT NOTES:
            1. Overtake: There may be typos in the Titles in the page info list. (e.g. "프렌치 프라이" and "프렌치프라이" are treated the same) 
            2. Allow partial matching: If the title contains an entry in the ui text list, it will be considered a match (e.g. "휘핑크림" and "휘핑크림추가")
            3. Simple Text Comparison: Determine only if the text matches purely, not semantic relationships (e.g., menus vs. categories).
            4. One-to-one matching: Each tile in the page info list can only match one item in the ui text list. Once used in a match, the Titles cannot be duplicated with another.
            
            score: The percentage of all items in the ui text list found in the page info list. It has a value between 0.0 (none at all) and 1.0 (all exist).
            contain: true if all of the ui text list is included, or false.
            
            One Example(
                ui text list: [
                    "아메리카노", "프렌치 프라이", "21치즈스틱", "코카콜라(R)"
                ],
                page info list: [
                    {"coordinate": [210, 364], "title": "크리스퍼 클래식 세트"},
                    {"coordinate": [67, 90], "title": "크리스퍼 클래식 라지세트"},
                    {"coordinate": [123, 87], "title": "주문하기"},
                    {"coordinate": [90, 456], "title": "추천메뉴"},
                    {"coordinate": [90, 456], "title": "사이드"},
                    {"coordinate": [120, 74], "title": "크리스퍼 클래식"},
                    {"coordinate": [8, 97], "title": "확인"},
                ]):
            ```json
            {
                "score": 0.0,
                "contain": false
            }
            ```
            
            Another Example(
                 ui text list: [
                    "아메리카노", "프렌치 프라이", "21치즈스틱", "코카콜라(R)"
                ],
                page info list: [
                    {"coordinate": [210, 364], "title": "프랜치프라이나"},
                    {"coordinate": [67, 90], "title": "취소"},
                    {"coordinate": [123, 87], "title": "아메리카노"},
                    {"coordinate": [90, 456], "title": "완료"},
                    {"coordinate": [90, 456], "title": "스프라이트R"}
                    {"coordinate": [120, 74], "title": "코카콜라"},
                    {"coordinate": [8, 97], "title": "프렌치프라이"},
                    {"coordinate": [8, 97], "title": "21치즈스칙"},
                ]):
            )
            ```json
            {
                "score": 1.0,
                "contain": true
            }
            ```
            
            Another Example(
                 ui text list: [
                    "간얼음", "HOT", "샷추가", "휘핑크림", "각얼음"
                ],
                page info list: [
                    {"coordinate": [210, 364], "title": "각얼음"},
                    {"coordinate": [67, 90], "title": "휘핑크림추가"},
                    {"coordinate": [123, 87], "title": "샷추가"},
                    {"coordinate": [90, 456], "title": "ICED"},
                    {"coordinate": [90, 456], "title": "취소"},
                    {"coordinate": [120, 74], "title": "간얼음"},
                    {"coordinate": [8, 97], "title": "아메리카노"},
                    {"coordinate": [8, 97], "title": "선택완료"},
                ]):
            )
            ```json
            {
                "score": 0.8,
                "contain": false
            }
            ```
            
            CRITICAL OUTPUT INSTRUCTION:
            Your final response MUST be a single, raw JSON object. Do NOT include any introductory text, explanations, or markdown formatting like \\\json ... \\\ around the JSON object.
        """.trimIndent()

        return prompt
    }
}