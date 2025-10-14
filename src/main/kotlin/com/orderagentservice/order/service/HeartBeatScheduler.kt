package com.orderagentservice.order.service

import com.orderagentservice.order.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.compareTo

@Component
class HeartBeatScheduler @Autowired constructor(
    private val notificationService: NotificationService,
    private val notificationRepository: NotificationRepository
) : ApplicationRunner {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val CHECK_INTERVAL: Long = 10_000

    override fun run(args: ApplicationArguments?) {
        scope.launch {
            while (isActive) {
                checkConnections()
                delay(CHECK_INTERVAL)
            }
        }
    }

    private fun checkConnections() {
        val kiosks = notificationRepository.getAllEmitter()
        for ((kioskId, emitter) in kiosks) {
            // 키오스크에게 확인 메시지
            notificationService.sendCheckCommand(kioskId)
        }
    }
}