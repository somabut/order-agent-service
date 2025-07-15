package com.orderagentservice.order

import com.orderagentservice.order.exception.CommandTimeoutException
import com.orderagentservice.order.repository.NotificationRepository
import com.orderagentservice.order.service.NotificationService
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import java.lang.reflect.InvocationTargetException

@SpringBootTest
class NotificationServiceTest @Autowired constructor(
    private val notificationService: NotificationService,
    private val notificationRepository: NotificationRepository
) {
    @Test
    fun `명령이 완료되면 파일을 가져온다`() {
        //when: private를 테스트를 위해 임시로 public으로 열고 파일을 저장한다
        val method = notificationService.javaClass.getDeclaredMethod("findAvailableKey", String::class.java)
        method.isAccessible = true
        notificationRepository.saveCaptureCommand("moodTRBL", File("moodTRBL"))

        //when: 명령완료를 기다린다
        val result = method.invoke(notificationService, "moodTRBL") as File

        //then: 파일이 정상적으로 들어온다
        assertThat(result.path).isEqualTo("moodTRBL")
    }

    @Test
    fun `명령이 완료되지 않았다면 예외를 반환한다`() {
        //when: private를 테스트를 위해 임시로 public으로 연다
        val method = notificationService.javaClass.getDeclaredMethod("waitCaptureCommand", String::class.java)
        method.isAccessible = true

        //then: 명령완료를 기다리다 시간초과로 예외 발생
        assertThatThrownBy { method.invoke(notificationService, "moodTRBL") as File }
            .isInstanceOf(InvocationTargetException::class.java)
            .hasCauseInstanceOf(CommandTimeoutException::class.java)
    }
}