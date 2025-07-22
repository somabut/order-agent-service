package com.orderagentservice.global

import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderOption
import com.orderagentservice.order.model.request.AutoOrderRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LogTest @Autowired constructor(
    private val globalLogger: GlobalLogger
) {
    private val autoOrderRequest = AutoOrderRequest(
        autoOrderMenus = listOf(
            AutoOrderMenu(
                category = "버거",
                title = "크리스퍼 클래식",
                count = 2,
                autoOrderOptions = listOf(
                    AutoOrderOption(title = "치즈 추가", count = 1),
                    AutoOrderOption(title = "베이컨 추가", count = 1)
                )
            ),
            AutoOrderMenu(
                category = "사이드",
                title = "감자튀김",
                count = 1,
                autoOrderOptions = listOf(
                    AutoOrderOption(title = "케첩", count = 2)
                )
            ),
            AutoOrderMenu(
                category = "음료",
                title = "콜라",
                count = 2,
                autoOrderOptions = emptyList()
            )
        ),
        place = "매장",
        payment = "카드"
    )

    @Test
    fun `자동 주문중에 로그를 기록한다`() {
        val json = jsonMapper.writeValueAsString(autoOrderRequest)
        println(json)
    }
}