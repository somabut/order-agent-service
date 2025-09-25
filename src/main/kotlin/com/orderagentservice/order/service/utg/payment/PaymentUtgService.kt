package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.log.UtgEndLog
import com.orderagentservice.order.model.log.UtgStartLog
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.model.type.UtgForLogType
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PaymentUtgService @Autowired constructor(
    private val paymentNavigator: PaymentNavigator,
    private val paymentEditor: PaymentEditor,
    private val logService: LogService,
    private val graphService: UiGraphService,
    private val usageTracker: UsageTracker,
) {
    fun initializeGraph(context: UtgContext) {
        logService.printLog(
            UtgStartLog(
                kioskId = context.kioskId,
                utgForLogType = UtgForLogType.PAYMENT
            )
        )
        val startTime = System.nanoTime()

        paymentNavigator.processPayment(context)

        val endTime = System.nanoTime()
        logService.printLog(
            UtgEndLog(
                kioskId = context.kioskId,
                utgForLogType = UtgForLogType.PAYMENT,
                processingTime = (endTime - startTime) / 1000000,
                totalTokenUsage = usageTracker.totalUsage
            )
        )
    }

    fun updatePayment(context: UtgContext) {
        val uiDtoList = graphService.findModified(context.kioskId)

        val modifiedPayment = uiDtoList
            .filter { it.type == NodeType.PAYMENT }
            .map { it.title }
            .first()

        paymentEditor.editPayment(context, modifiedPayment)

        graphService.changeModified(context.kioskId, modifiedPayment)
    }
}