package com.orderagentservice.global.util

import org.springframework.stereotype.Component

interface Sleeper {
    fun sleep(mills: Long)
}

@Component
class SleeperImpl : Sleeper {
    override fun sleep(mills: Long) {
        Thread.sleep(mills)
    }
}