package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.AgentPageDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PageAgent @Autowired constructor(
    private val llmManager: LlmManager
) {
    fun determineAction(metaList: List<String>, uiList: List<LlmUiComponentDto>): AgentPageDto {
        val prompt = getPrompt(metaList, uiList)
        val json = llmManager.queryGemini(prompt)
        val response: AgentPageDto = jsonMapper.readValue<AgentPageDto>(json)
        return response
    }

    private fun getPrompt(metaList: List<String>, uiList: List<LlmUiComponentDto>): String {
        val prompt = """
            You are an expert who looks at a given ui text list and compares it to the current page information to find out if the ui text list is a component of the current page.
            The information about the page is on the page info list. In other words, the page info list is the information on the current page.
            The response should be in Json format.
            
            Here is ui text list:${metaList}
            Here is page info list:${uiList}
            
            IMPORTANT NOTES:
            1. If the ui text list means the current page, the information should be included in the 'title' of the page info list.
            2. The 'title' of the page info list may have a typo, so you have to take that into account.
            3. If most of the ui text list is included, it's the current page. If only part of it is included in page info list, it's not the current page.
            5. The UI element match is to ask if the text matches, not like the relationship between the menu and the category. 
            
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
                "score": 1.0,
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
                "contain": true
            }
            ```
        """.trimIndent()

        return prompt
    }
}