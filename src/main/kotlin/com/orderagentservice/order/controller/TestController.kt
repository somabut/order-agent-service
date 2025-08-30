package com.orderagentservice.order.controller

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.order.model.type.OverlayType
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.service.utg.WordSimilarityService
import com.orderagentservice.order.service.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController @Autowired constructor(
    private val notificationService: NotificationService,
    private val wordSimilarityService: WordSimilarityService,
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

    @GetMapping("/test/action/{kioskId}")
    fun sendActionMessage(
        @PathVariable kioskId: String,
        @RequestParam x: Int, @RequestParam y: Int
    ) {
        notificationService.sendActionCommand(kioskId, CoordinateDto(x = x, y = y, title = "TEST"))
    }

    @GetMapping("/test/capture/{kioskId}")
    fun sendCaptureMessage(@PathVariable kioskId: String) {
        notificationService.sendCaptureCommand(kioskId)
    }

    @GetMapping("/test/overlay/{kioskId}")
    fun sendOverlayMessage(@PathVariable kioskId: String) {
        notificationService.sendOverlayCommand(kioskId, OverlayType.UTG_START.title)
    }

    @GetMapping("/test/compare")
    fun compareWord(): WordMatchDto {
        val optUiList = listOf(
            UiComponentDto(x = 42, y = 31, title = "사이드"),
            UiComponentDto(x = 73, y = 31, title = "메뉴"),
            UiComponentDto(x = 76, y = 142, title = "빽스치노"),
            UiComponentDto(x = 197, y = 142, title = "딸기빽스치노"),
            UiComponentDto(x = 317, y = 142, title = "녹차빽스치노"),
            UiComponentDto(x = 197, y = 163, title = "초코빽스치노"),
            UiComponentDto(x = 319, y = 163, title = "+300뭔"),
            UiComponentDto(x = 34, y = 432, title = "음료"),
            UiComponentDto(x = 75, y = 543, title = "고카콜라R"),
            UiComponentDto(x = 196, y = 543, title = "스프라이트R"),
            UiComponentDto(x = 198, y = 564, title = "100원"),
            UiComponentDto(x = 56, y = 835, title = "총주문금액"),
            UiComponentDto(x = 464, y = 835, title = "13500원"),
            UiComponentDto(x = 138, y = 884, title = "취소"),
            UiComponentDto(x = 383, y = 884, title = "완료")
        )
        val target = "녹차 빽스치노"

        return wordSimilarityService.findBestMatch(target, optUiList)
    }

    @GetMapping("/test/determine")
    fun determineWord(): Boolean {
        val sourceList = listOf(
            "달콤아이스티",
            "초코라떼",
            "우리수박주스(ICED)",
        )
        val uiList = listOf(
            UiComponentDto(x = 370, y = 80, title = "음료"),
            UiComponentDto(x = 106, y = 78, title = "커피"),
            UiComponentDto(x = 870, y = 77, title = "백스치노"),
            UiComponentDto(x = 365, y = 313, title = "PAIK'S"),
            UiComponentDto(x = 370, y = 327, title = "COFFEE"),
            UiComponentDto(x = 294, y = 386, title = "우리수박주스(ICED)"),
            UiComponentDto(x = 696, y = 1766, title = "전체취소"),
            UiComponentDto(x = 71, y = 381, title = "달콤아이스티"),
            UiComponentDto(x = 623, y = 313, title = "PAIK'S"),
            UiComponentDto(x = 638, y = 329, title = "OEFEE"),
            UiComponentDto(x = 607, y = 380, title = "초코라떼"),
            UiComponentDto(x = 659, y = 440, title = "W 3,500"),
            UiComponentDto(x = 620, y = 78, title = "디저트"),
        )

        return wordSimilarityService.determinePage(sourceList, uiList)
    }
}