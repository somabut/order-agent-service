package com.orderagentservice.order.controller

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.service.WordSimilarityService
import com.orderagentservice.order.service.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController @Autowired constructor(
    private val notificationService: NotificationService,
    private val wordSimilarityService: WordSimilarityService
) {
    @GetMapping("/")
    fun test(): String {
        return "ok"
    }

    @GetMapping("/test")
    fun sendMessageTest() {
        notificationService.broadcastMessage("test message from moodTRBL")
    }

    @GetMapping("/test/{kioskId}")
    fun sendMessageTest(@PathVariable kioskId: String) {
        notificationService.sendMessage(kioskId, "[${kioskId}] test message from moodTRBL")
    }

    @GetMapping("/test/compare")
    fun compareWord(): WordMatchDto {
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

        return wordSimilarityService.findBestMatch(target, optUiList)
    }

    @GetMapping("/test/determine")
    fun determineWord(): Boolean {
        val sourceList = listOf(
            "크리스퍼 클래식",
            "통모짜와퍼세트",
            "BBQ 통모짜와퍼",
        )
        val uiList = listOf(
            LlmUiComponentDto(x = -1, y = -1, title = "통모짜와퍼"),
            LlmUiComponentDto(x = -1, y = -1, title = "코카콜라"),
            LlmUiComponentDto(x = -1, y = -1, title = "프렌치프라이"),
            LlmUiComponentDto(x = -1, y = -1, title = "카트담기"),
            LlmUiComponentDto(x = -1, y = -1, title = "크리스퍼클래식"),
            LlmUiComponentDto(x = -1, y = -1, title = "취소"),
        )

        return wordSimilarityService.determinePage(sourceList, uiList)
    }
}