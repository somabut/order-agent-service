package com.orderagentservice.order.service.utg.payment

import com.orderagentservice.logger
import com.orderagentservice.order.model.GraphContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentUtgService @Autowired constructor(
    private val paymentNavigator: PaymentNavigator
) {
    private val log = logger()

    @Transactional
    fun initializeGraph(context: GraphContext) {
        log.info("결제 utg 생성 시작")
        val startTime = System.nanoTime()

        paymentNavigator.processPayment(context)

        val endTime = System.nanoTime()
        log.info("결제 utg 생성 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }
}