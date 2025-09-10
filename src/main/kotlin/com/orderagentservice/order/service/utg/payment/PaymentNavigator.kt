package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.order.exception.UtgInfiniteLoopException
import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentNavigator @Autowired constructor(
    private val placeUtgService: PlaceUtgService,
    private val paymentActionExecutor: PaymentActionExecutor,
    private val paymentNodeGenerator: PaymentNodeGenerator,
    private val uiDetectorManager: UiDetectorManager
) {
    private val MAX_LOOP = 5

    fun processPayment(context: GraphContext) {
        var loopTime = 0

        while (loopTime <= MAX_LOOP) {
            //포장/매장 UI 확인
            val uiList = uiDetectorManager.getUiComponents(context).ocrElements

            if (context.isPlaceDetermined == false) {
                placeUtgService.initializeGraph(context, uiList)
            }
            val paymentEnd = paymentActionExecutor.selectPayment(context, uiList)
            if (paymentEnd == false) {
                paymentNodeGenerator.createCompleteNode(context)
                return
            }
            loopTime += 1
        }

        throw UtgInfiniteLoopException()
    }
}