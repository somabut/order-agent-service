package com.orderagentservice.global.util

import org.springframework.stereotype.Component

interface Timer {
    fun getCurrentTimeMillis(): Long
}

@Component
class TimerImpl : Timer {
    override fun getCurrentTimeMillis() = System.currentTimeMillis()
}