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
            "우리수박주스",
        )

        val uiList = listOf(
            UiComponentDto(x = 405, y = 98, title = "음료"),
            UiComponentDto(x = 134, y = 98, title = "커피"),
            UiComponentDto(x = 139, y = 416, title = "달콤아이스티"), //
            UiComponentDto(x = 198, y = 476, title = "W2,800"),
            UiComponentDto(x = 780, y = 1833, title = "전체취소"),
            UiComponentDto(x = 944, y = 96, title = "백스치노"),
            UiComponentDto(x = 366, y = 1839, title = "1"),
            UiComponentDto(x = 368, y = 1842, title = "+"),
            UiComponentDto(x = 173, y = 1839, title = "1"),
            UiComponentDto(x = 174, y = 1842, title = "+"),
            UiComponentDto(x = 403, y = 335, title = "PAIK'S"),
            UiComponentDto(x = 405, y = 347, title = "COFFEE"),
            UiComponentDto(x = 407, y = 415, title = "우리수박주스"), //
            UiComponentDto(x = 405, y = 456, title = "(ICED)"),
            UiComponentDto(x = 673, y = 339, title = "PAIK'S"),
            UiComponentDto(x = 678, y = 351, title = "OIFEE"),
            UiComponentDto(x = 672, y = 414, title = "초코라떼"),   //
            UiComponentDto(x = 730, y = 475, title = "W3,500"),
            UiComponentDto(x = 565, y = 1838, title = "1"),
            UiComponentDto(x = 564, y = 1841, title = "+"),
            UiComponentDto(x = 999, y = 1773, title = "결제하기"),
            UiComponentDto(x = 675, y = 97, title = "디저트"),
        )

        return wordSimilarityService.determinePage(sourceList, uiList)
    }
}