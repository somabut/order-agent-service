package com.orderagentservice.order.service.utg.sequencer

import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.payment.PaymentNodeIntegrator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentActionSequencer @Autowired constructor(
    private val uiDetectorManager: UiDetectorManager,
    private val paymentNodeIntegrator: PaymentNodeIntegrator
) {
    private val PAYMENT_MAX_LOOP = 5

    fun run(
        context: UtgContext,
        actionProfile: UtgActionProfile,
    ): Boolean {
        var loopTime = 0
        while (loopTime <= PAYMENT_MAX_LOOP) {
            val uiList = uiDetectorManager.getUiComponents(context).ocrElements

            val paymentEnd = actionProfile.paymentSelectStrategy.execute(context, uiList)
            if (paymentEnd == false) {
                paymentNodeIntegrator.integrateCompleteNode(context)
                return true
            }
            loopTime += 1
        }

        return false
    }
}