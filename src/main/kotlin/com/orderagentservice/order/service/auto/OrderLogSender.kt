package com.orderagentservice.order.service.auto

import com.orderagentservice.global.model.dto.LogDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class OrderLogSender @Autowired constructor(
    private val logService: LogService
) {
    private val log = logger()

    fun<T> logOrder(kioskId: String, taskId: String, message: String, obj: T) {
        val logDto = LogDto(
            kioskId = kioskId,
            taskId = taskId,
            message = message
        )
        val json = jsonMapper.writeValueAsString(logDto)

        log.info(message)
        logService.sendLog(json)
        logService.printLog(obj)
    }
}