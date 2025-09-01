package com.orderagentservice.global

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.service.utg.WordSimilarityService
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
            UiComponentDto(x = 42, y = 31, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "사이드"),
            UiComponentDto(x = 73, y = 31, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "메뉴"),
            UiComponentDto(x = 76, y = 142, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "빽스치노"),
            UiComponentDto(x = 197, y = 142, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "딸기빽스치노"),
            UiComponentDto(x = 317, y = 142, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "녹차빽스치노"),
            UiComponentDto(x = 197, y = 163, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "초코빽스치노"),
            UiComponentDto(x = 319, y = 163, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "+300뭔"),
            UiComponentDto(x = 34, y = 432, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "음료"),
            UiComponentDto(x = 75, y = 543, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "고카콜라R"),
            UiComponentDto(x = 196, y = 543, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "스프라이트R"),
            UiComponentDto(x = 198, y = 564, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "100원"),
            UiComponentDto(x = 56, y = 835, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "총주문금액"),
            UiComponentDto(x = 464, y = 835, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "13500원"),
            UiComponentDto(x = 138, y = 884, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "취소"),
            UiComponentDto(x = 383, y = 884, minX = 1, minY = 1, maxX = 1, maxY = 1,title = "완료")
        )
        val target = "녹차 빽스치노"

        val result = wordSimilarityService.findBestMatch(target, optUiList)

        println(result)
    }
}