package com.orderagentservice.global.util

import com.orderagentservice.global.model.LogType
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.auto.model.AutoOrderResultDto
import com.orderagentservice.order.utg.model.dto.CoordinateDto
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component
class GlobalLogger {
    private val LOGGER_NAME = "AUTO_ORDER"
    private val logger = LoggerFactory.getLogger(LOGGER_NAME)

    fun loggingOrderStart(kioskId: String, taskId: String) {
        MDC.put("logType", LogType.ORDER_START.name)
        MDC.put("kioskId", kioskId)
        MDC.put("taskId", taskId)
        logger.info(LogType.ORDER_START.message)
        MDC.clear()
    }

    fun loggingActionResult(
        kioskId: String, commandId: String,
        actionType: String, success: Boolean,
        coordinate: CoordinateDto
    ) {
        MDC.put("logType", LogType.ACTION_RESULT.name)
        MDC.put("kioskId", kioskId)
        MDC.put("commandId", commandId)
        MDC.put("actionType", actionType)
        MDC.put("success", success.toString())
        MDC.put("title", coordinate.title)
        MDC.put("coordinate", listOf(coordinate.x, coordinate.y).toString())
        logger.info("${LogType.ACTION_RESULT.message} ${success}!! -> title: ${coordinate.title}, coordinate: ${coordinate.x} ${coordinate.y}")
        MDC.clear()
    }

    fun loggingOrderResult(
        kioskId: String, menuList: AutoOrderResultDto,
        processingTime: Long, paymentMethod: String,
        taskId: String
    ) {
        val menuJson = jsonMapper.writeValueAsString(menuList)

        MDC.put("logType", LogType.ORDER_RESULT.name)
        MDC.put("kioskId", kioskId)
        MDC.put("taskId", taskId)
        MDC.put("menuList", menuJson)
        MDC.put("processingTime (ms)", processingTime.toString())
        MDC.put("paymentMethod", paymentMethod)
        logger.info(LogType.ORDER_RESULT.message)
        MDC.clear()
    }
}