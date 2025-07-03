package com.orderagentservice.order

import com.orderagentservice.order.exception.NoSuchKioskException
import com.orderagentservice.order.repository.NotificationRepository
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class NotificationRepositoryTest @Autowired constructor(
    private val notificationRepository: NotificationRepository
) {
    @Test
    fun `emitter가 없으면 예외를 발생시킨다`() {
        assertThatThrownBy { notificationRepository.getEmitter("moodTRBL") }
            .isInstanceOf(NoSuchKioskException::class.java)
    }
}