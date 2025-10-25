package com.orderagentservice.agent.util

import org.springframework.stereotype.Component

interface Sleeper {
    fun sleep(mills: Long)
}

@Component
class ProductionSleeper : Sleeper {
    override fun sleep(mills: Long) {
        Thread.sleep(mills)
    }
}