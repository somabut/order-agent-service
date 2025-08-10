package com.orderagentservice.agent

import com.orderagentservice.logger
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class NextUiBenchMarkTest @Autowired constructor(
    private val backAgent: BackAgent,
    private val paymentAgent: PaymentAgent
) : AbstractNextUiBenchMarkTest() {
    private val log = logger()

    @Test
    fun `backAgent 벤치마크`() {
        var count = 0
        for (i in backBenchList.indices) {
            val result = backAgent.determineAction(backBenchList[i])
            if (result.title == backAnswerList[i]) count += 1
            Thread.sleep(2000)
        }

        log.info("정확도: ${count.toFloat() / backBenchList.size}, count: $count")
    }

    @Test
    fun `paymentAgent 벤치마크`() {
        var count = 0
        for (i in paymentBenchList.indices) {
            val result = paymentAgent.determineAction(paymentBenchList[i])
            if (result.title == paymentAnswerList[i]) count += 1
            else log.info("틀림 ${paymentAnswerList[i]}대신 ${result.title} 선택")
            Thread.sleep(2000)
        }

        log.info("정확도: ${count.toFloat() / paymentBenchList.size}, count: $count")
    }
}