package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.order.model.GraphContext
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PaymentNavigator @Autowired constructor(
    private val placeUtgService: PlaceUtgService,
    private val paymentActionExecutor: PaymentActionExecutor,
    private val paymentNodeGenerator: PaymentNodeGenerator
) {
    fun processPayment(context: GraphContext) {
        while (true) {
            //포장/매장 UI 확인
            if (context.isPlaceDetermined == false) {
                placeUtgService.initializeGraph(context)
            }
            val paymentEnd = paymentActionExecutor.selectPayment(context)
            if (paymentEnd == false) break
        }
        paymentNodeGenerator.createCompleteNode(context)
    }
}