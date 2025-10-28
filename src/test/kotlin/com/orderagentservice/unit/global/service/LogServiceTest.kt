package com.orderagentservice.unit.global.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.orderagentservice.global.exception.InvalidSessionException
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.global.service.LogService
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.logging.Logger

@ExtendWith(MockKExtension::class)
class LogServiceTest {
    @MockK
    private lateinit var env: Environment

    @MockK
    private lateinit var mockMapper: ObjectMapper

    private lateinit var logService: LogService

    private val MOCK_HOST = "http://mock-chat.host"
    private val MOCK_URL = "http://mock-chat.host/v1/task/"
    private val MOCK_MESSAGE = "{\"key\":\"test_message\"}"
    private val MOCK_API_RESPONSE = ApiResponse<Any>(
        success = false, data = null, error = null, httpStatus = null
    ) // 더미 응답 객체

    private val MOCK_JSON_STRING = "{\"id\":123,\"action\":\"order\"}"

    private val MOCK_401_ERROR = HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized")

    @BeforeEach
    fun setUp() {
        mockkStatic(::jsonMapper)
        every { jsonMapper } returns mockMapper
        every { mockMapper.writeValueAsString(any()) } returns MOCK_JSON_STRING

        mockkConstructor(RestTemplate::class)
        every { env.getProperty("order-chat.host") } returns MOCK_HOST

        logService = LogService(env)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @DisplayName("sendLog가 성공하면 RestTemplate.exchange를 호출하고 ApiResponse를 반환한다")
    fun `sendLog should call exchange and return ApiResponse on success`() {
        // given
        val mockResponseEntity = mockk<ResponseEntity<ApiResponse<*>>>()
        every { mockResponseEntity.body } returns MOCK_API_RESPONSE

        every {
            constructedWith<RestTemplate>().exchange(
                eq(MOCK_URL),                // URL 검증
                eq(HttpMethod.POST),         // HttpMethod 검증
                any<HttpEntity<String>>(),   // HttpEntity 검증 (더 상세하게 가능)
                eq(ApiResponse::class.java)  // 반환 타입 클래스 검증
            )
        } returns mockResponseEntity

        // when
        val actualResponse = logService.sendLog(MOCK_MESSAGE)

        // then
        assertThat(actualResponse).isSameAs(MOCK_API_RESPONSE)
    }

    @Test
    @DisplayName("sendLog가 HttpClientErrorException 발생 시 InvalidSessionException을 던진다")
    fun `sendLog should throw InvalidSessionException on HttpClientErrorException`() {
        // given
        every {
            constructedWith<RestTemplate>().exchange(
                any<String>(),
                any<HttpMethod>(),
                any<HttpEntity<String>>(),
                any<Class<*>>()
            )
        } throws MOCK_401_ERROR

        // when & then
        assertThrows<InvalidSessionException> {
            logService.sendLog(MOCK_MESSAGE)
        }
    }
}