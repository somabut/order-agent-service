package com.orderagentservice.global

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.global.service.WordSimilarityService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class WordSimilarityService @Autowired constructor(
    private val wordSimilarityService: WordSimilarityService
) {
    @Test
    fun `유사도 검색으로 단어를 찾는다`() {
        val optUiList = listOf(
            LlmUiComponentDto(x = 42, y = 31, title = "사이드"),
            LlmUiComponentDto(x = 73, y = 31, title = "메뉴"),
            LlmUiComponentDto(x = 76, y = 142, title = "빽스치노"),
            LlmUiComponentDto(x = 197, y = 142, title = "딸기빽스치노"),
            LlmUiComponentDto(x = 317, y = 142, title = "녹차빽스치노"),
            LlmUiComponentDto(x = 197, y = 163, title = "초코빽스치노"),
            LlmUiComponentDto(x = 319, y = 163, title = "+300뭔"),
            LlmUiComponentDto(x = 34, y = 432, title = "음료"),
            LlmUiComponentDto(x = 75, y = 543, title = "고카콜라R"),
            LlmUiComponentDto(x = 196, y = 543, title = "스프라이트R"),
            LlmUiComponentDto(x = 198, y = 564, title = "100원"),
            LlmUiComponentDto(x = 56, y = 835, title = "총주문금액"),
            LlmUiComponentDto(x = 464, y = 835, title = "13500원"),
            LlmUiComponentDto(x = 138, y = 884, title = "취소"),
            LlmUiComponentDto(x = 383, y = 884, title = "완료")
        )
        val target = "녹차 빽스치노"

        val result = wordSimilarityService.findBestMatch(target, optUiList)

        println(result)
    }
}