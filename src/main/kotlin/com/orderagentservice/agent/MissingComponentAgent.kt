package com.orderagentservice.agent

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.agent.model.dto.AgentActionDto
import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

@Component
class MissingComponentAgent@Autowired constructor(
    private val llmManager: LlmManager
) {
    fun determineAction(image: File, titleList: List<String>, uiList: List<LlmUiComponentDto>): List<LlmUiComponentDto> {
        val prompt = getPrompt(image, titleList, uiList)
        val json = llmManager.queryGemini(prompt)
        val response: List<LlmUiComponentDto> = jsonMapper.readValue(
            json,
            object : TypeReference<List<LlmUiComponentDto>>() {}
        )
        return response
    }

    private fun getPrompt(image: File, titleList: List<String>, uiList: List<LlmUiComponentDto>): String {
        val prompt = """
            You are an expert at figuring out the pixel coordinates of the text on a given ui list in a given image.
            Find the pixel coordinates of the UI that are on the `title list` but not on the `ui list`.
            The response should be in Json format.

            Here is request input image: $image
            Here is title list: $titleList
            Here is ui list: $uiList

            IMPORTANT NOTES:
            1. The criterion for determining whether it is the same UI is whether the elements of 'title' and 'title list' match.
            2. You shouldn't judge similar words to be the same, but there may be minor typos on the 'ui list', so take this into account. 
            3. Pixel coordinates should be judged as accurately as possible. You can refer to other UIs on the 'ui list' for accurate judgment. 
            In other words, if what you're trying to find isn't on the 'ui list', you have to infer on the same column/row basis. 
            [ex) If b exists between a and c, we can judge that b exists in the coordinate between a and c.]
            4. If there is a (+) after the name in the 'title list', you need to find the '+' button to choose the quantity. not the pixel coordinates of the string. [ex)If it's "면추가 (+)", not "면추가"]
            5. The information that can be compared('title list') at this time is not tied together like 'title +', but 'title' and '+' buttons come in separately, so you have to tell me the coordinates of the + button.
            6. '+' Infer the coordinates of the button by referring to Rule 3.
            7. These coordinates have to be really accurate, so we have to think carefully about it and respond.
            8. Make sure to return the 'title' in the response to those in the title list.
            
            `coordinate` is the coordinate of the UI found because a specific UI is not in the `ui list`, and the `title` is the title of the UI with that coordinate.
            
            The criteria for the 'score' are as follows. 
                "I'm sure I got it right" → 1.0
                "Meaning or context is correct, but not 100% sure" → 0.7~0.9
                "Looks similar but uncertain" → 0.5~0.6
                "Almost not sure" → 0~0.4
            
            One Example(
                title_list: ["샷추가, 휘핑크림 추가", "펄 추가"]),
                ui_list: [
                    {"coordinate": [210, 364], "title": "아메라카노 5000원-"},
                    {"coordinate": [67, 90], "title": "카페라떼 5400원-"},
                    {"coordinate": [123, 87], "title": "카라멜 마끼야또 5000원-"},
                    {"coordinate": [90, 456], "title": "샷추가"},
                    {"coordinate": [120, 74], "title": "담기"},
                    {"coordinate": [456, 34], "title": "완료"}
                ]):
            ```json
            [
                {"coordinate": [90, 421], "title": "휘핑크림 추가"},
                {"coordinate": [90, 487], "title": "펄 추가"}
            ]
            ```
            
            Another Example(
                title_list: ["면추가 (+)", "차슈 (+)", "멘마 (+)"]),
                ui_list: [
                    {"coordinate": [150, 120], "title": "+"},
                    {"coordinate": [150, 87], "title": "+"},
                    {"coordinate": [123, 87], "title": "멘마"},
                    {"coordinate": [123, 120], "title": "차슈"},
                    {"coordinate": [123, 164], "title": "면 추가"},
                    {"coordinate": [456, 34], "title": "취소"}
                ]):
            ```json
            [
                {"coordinate": [150, 164], "title": "면추가 (+)"}
            ]
            ```
            
            Another Example(
                title_list: ["나타드코코추가 (+)", "아이스크림 (+)", "휘핑크림추가 (+)", "샷추가 (+)", "간얼음"]),
                ui_list: [
                    {"coordinate": [123, 185], "title": "나타드코코추가"},
                    {"coordinate": [187, 185], "title": "+"},
                    {"coordinate": [123, 87], "title": "아이스크림"},
                    {"coordinate": [123, 120], "title": "휘핑크림추가"},
                    {"coordinate": [123, 164], "title": "샷추가"},
                    {"coordinate": [187, 164], "title": "+"},
                    {"coordinate": [257, 455], "title": "간얼음"}
                ]):
            ```json
            [
                {"coordinate": [187, 87], "title": "아이스크림 (+)"},
                {"coordinate": [187, 120], "title": "휘핑크림추가 (+)"}
            ]
            ```            
        """.trimIndent()

        return prompt
    }
}