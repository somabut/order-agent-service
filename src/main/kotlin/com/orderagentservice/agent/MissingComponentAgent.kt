package com.orderagentservice.agent

import com.fasterxml.jackson.core.type.TypeReference
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
        val json = llmManager.query(prompt)
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
            1. Find UI elements that are NOT in the current ui list but exist in the title list.
            2. You shouldn't judge similar words to be the same, but there may be minor typos on the 'ui list', so take this into account.
            3. COORDINATE INFERENCE: When the target element is not in ui list:
               - Analyze the layout pattern of similar elements in ui list
               - Infer coordinates based on spatial relationships (relative positioning, alignment, spacing)
               - Consider UI design patterns (buttons are usually aligned, menus have consistent spacing)
             
            4. QUANTITY SELECTION: If target has "(+)" suffix [ex)If it's "면추가 (+)", not "면추가"]:
               - Find the "+" button near the base element name
               - Return coordinates of the "+" button, NOT the text coordinates
               - The "+" button is usually located on the left and right sides of the element name
               
            5. Return the exact title from the target list, not from ui list.
            6. These coordinates have to be really accurate, so we have to think carefully about it and respond.
            
            `coordinate` is the coordinate of the UI found because a specific UI is not in the `ui list`, and the `title` is the title of the UI with that coordinate.            
            
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