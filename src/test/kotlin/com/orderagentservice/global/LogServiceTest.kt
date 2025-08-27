package com.orderagentservice.global

import com.orderagentservice.global.model.dto.LogDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.model.log.UtgProcessLog
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LogServiceTest @Autowired constructor(
    private val logService: LogService
) {
    @Test
    fun `로그를 chat service에게 보낸다`() {
        val logDto = LogDto(
            kioskId = "kiosk1",
            taskId = "test-session-1",
            message = "로그 테스트입니다"
        )
        val message = jsonMapper.writeValueAsString(logDto)
        val response = logService.sendLog(message)
        println(response)
    }

    @Test
    fun `로그를 프린트한다`() {
        val dto = UtgProcessLog(
            kioskId = "test",
            message = "안녕하세요"
        )
        logService.printLog(dto)
    }
}