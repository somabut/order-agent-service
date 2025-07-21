package com.orderagentservice.global

import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.service.UtgService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LearningTest @Autowired constructor(
    private val globalLogger: GlobalLogger,
    private val utgService: UtgService
) {
    @Test
    fun `asdfas`() {
        val rootUiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = "test",
            title = "root"
        )
        val result = utgService.saveNode(rootUiDto)
        println(result.version)
    }
}