package com.orderagentservice.order.util

import com.orderagentservice.global.model.LogType
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.OrderResultDto
import jdk.internal.classfile.components.ClassPrinter.toJson
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component
class GlobalLogger {
    private val LOGGER_NAME = "AUTO_ORDER"
    private val logger = LoggerFactory.getLogger(LOGGER_NAME)

    fun loggingOrderStart(kioskId: String) {
        MDC.put("logType", LogType.ORDER_START.name)
        MDC.put("kioskId", kioskId)
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
        logger.info(LogType.ACTION_RESULT.message)
        MDC.clear()
    }

    fun loggingOrderResult(
        kioskId: String, menuList: List<OrderResultDto>,
        processingTime: Long, paymentMethod: String
    ) {
        val menuJson = jsonMapper.writeValueAsString(menuList)
        val totalAmount = menuList.sumOf { it.quantity }

        MDC.put("logType", LogType.ORDER_RESULT.name)
        MDC.put("kioskId", kioskId)
        MDC.put("menuList", menuJson)
        MDC.put("processingTime (ms)", processingTime.toString())
        MDC.put("paymentMethod", paymentMethod)
        MDC.put("totalAmount", totalAmount.toString())
        logger.info(LogType.ORDER_RESULT.message)
        MDC.clear()
    }
}