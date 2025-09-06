package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.model.log.UtgEndLog
import com.orderagentservice.order.model.log.UtgProcessLog
import com.orderagentservice.order.model.log.UtgStartLog
import com.orderagentservice.order.model.type.UtgType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentUtgService @Autowired constructor(
    private val paymentNavigator: PaymentNavigator,
    private val paymentEditor: PaymentEditor,
    private val logService: LogService,
    private val usageTracker: UsageTracker
) {
    @Transactional
    fun initializeGraph(context: GraphContext) {
        logService.printLog(
            UtgStartLog(
                kioskId = context.kioskId,
                utgType = UtgType.PAYMENT
            )
        )
        val startTime = System.nanoTime()

        paymentNavigator.processPayment(context)

        val endTime = System.nanoTime()
        logService.printLog(
            UtgEndLog(
                kioskId = context.kioskId,
                utgType = UtgType.PAYMENT,
                processingTime = (endTime - startTime) / 1000000,
                totalTokenUsage = usageTracker.totalUsage
            )
        )
    }

    fun updatePayment(context: GraphContext, updatedUi: String) {
        paymentEditor.editPayment(context, updatedUi)
    }
}