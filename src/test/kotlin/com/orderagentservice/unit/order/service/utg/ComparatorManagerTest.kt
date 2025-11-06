package com.orderagentservice.unit.order.service.utg

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.global.util.ImageUtils
import com.orderagentservice.order.model.request.WordCompareRequest
import com.orderagentservice.order.model.response.ImageCompareResponse
import com.orderagentservice.order.model.response.WordCompareResponse
import com.orderagentservice.order.service.utg.ComparatorManager
import io.mockk.clearConstructorMockk
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@ExtendWith(MockKExtension::class)
class ComparatorManagerTest {
    @MockK
    private lateinit var env: Environment

    private lateinit var comparatorManager: ComparatorManager

    private val MOCK_COMPARATOR_HOST = "http://mock-comparator.com"

    @BeforeEach
    fun setUp() {
        every { env.getProperty("comparator.host") } returns MOCK_COMPARATOR_HOST
        comparatorManager = ComparatorManager(env)
        mockkObject(ImageUtils.Companion)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(ImageUtils.Companion)
        clearConstructorMockk(RestTemplate::class)
    }

    @Test
    @DisplayName("imageCompare: 이미지 비교 성공 시 true 반환")
    fun `imageCompare should return true on successful comparison`() {
        val sourceImg = byteArrayOf(1, 2, 3)
        val targetImg = byteArrayOf(4, 5, 6)
        val sourceType = "image/png"
        val targetType = "image/png"

        every { ImageUtils.getExtension(any()) } returns "png"

        mockkConstructor(RestTemplate::class)

        val mockCompareResponse = ImageCompareResponse(result = true)
        val mockApiResponse = ApiResponse(data = mockCompareResponse, success = true, error = null, httpStatus = null)
        val mockResponseEntity = ResponseEntity(mockApiResponse, HttpStatus.OK)

        every {
            anyConstructed<RestTemplate>().exchange(
                eq("$MOCK_COMPARATOR_HOST/v1/compare/image"),
                eq(HttpMethod.POST),
                any<HttpEntity<*>>(),
                any<ParameterizedTypeReference<ApiResponse<ImageCompareResponse>>>()
            )
        } returns mockResponseEntity

        // when
        val result = comparatorManager.imageCompare(sourceImg, sourceType, targetImg, targetType)

        // then
        assertTrue(result)
    }

    @Test
    @DisplayName("imageCompare: API 호출 실패 시 OrderAgentException 발생")
    fun `imageCompare should throw OrderAgentException on API failure`() {
        // given
        val sourceImg = byteArrayOf(1, 2, 3)
        val targetImg = byteArrayOf(4, 5, 6)
        val sourceType = "image/png"
        val targetType = "image/png"

        every { ImageUtils.getExtension(any()) } returns "png"
        mockkConstructor(RestTemplate::class)

        every {
            anyConstructed<RestTemplate>().exchange(
                any<String>(),
                any(),
                any(),
                any<ParameterizedTypeReference<ApiResponse<ImageCompareResponse>>>()
            )
        } throws RuntimeException("API is down")

        // when & then
        val exception = assertThrows(OrderAgentException::class.java) {
            comparatorManager.imageCompare(sourceImg, sourceType, targetImg, targetType)
        }

        assertEquals(ErrorCode.ORDER_IMAGE_COMPARE_FAIL, exception.errorCode)
    }

    @Test
    @DisplayName("wordCompare: 단어 비교 성공 시 WordMatchDto 반환")
    fun `wordCompare should return WordMatchDto on successful comparison`() {
        // given
        val targetWord = "Americano"
        val mockComponent = UiComponentDto(x = 1, y = 2, minX = 2, maxX = 2, minY = 2, maxY = 2, title = "Americano")
        val candidates = listOf(mockComponent, UiComponentDto(x = 1, y = 2, minX = 2, maxX = 2, minY = 2, maxY = 2, title = "Latte"))

        mockkConstructor(RestTemplate::class)

        // Mock API 응답 생성
        val expectedMatch = WordMatchDto(x = 1, y = 2, minX = 2, maxX = 2, minY = 2, maxY = 2, title = "Americano", score = 1.0)
        val mockCompareResponse = WordCompareResponse(result = expectedMatch)
        val mockApiResponse = ApiResponse(data = mockCompareResponse, success = true, error = null, httpStatus = null)
        val mockResponseEntity = ResponseEntity(mockApiResponse, HttpStatus.OK)

        // 요청 본문을 캡처하기 위한 slot
        val httpEntitySlot = slot<HttpEntity<WordCompareRequest>>()

        every {
            anyConstructed<RestTemplate>().exchange(
                eq("$MOCK_COMPARATOR_HOST/v1/compare/word"),
                eq(HttpMethod.POST),
                capture(httpEntitySlot), // 요청 캡처
                any<ParameterizedTypeReference<ApiResponse<WordCompareResponse>>>()
            )
        } returns mockResponseEntity

        // when
        val result = comparatorManager.wordCompare(targetWord, candidates)

        // then
        assertNotNull(result)
        assertEquals(expectedMatch, result)

        val capturedRequest = httpEntitySlot.captured.body
        assertNotNull(capturedRequest)
        assertEquals(targetWord, capturedRequest!!.target)
        assertEquals(candidates, capturedRequest.candidates)
    }

    @Test
    @DisplayName("wordCompare: API 호출 실패 시 OrderAgentException 발생")
    fun `wordCompare should throw OrderAgentException on API failure`() {
        // given
        val targetWord = "Americano"
        val candidates = listOf(UiComponentDto(x = 1, y = 2, minX = 2, maxX = 2, minY = 2, maxY = 2, title = "Americano"))

        mockkConstructor(RestTemplate::class)

        every {
            anyConstructed<RestTemplate>().exchange(
                any<String>(),
                any(),
                any(),
                any<ParameterizedTypeReference<ApiResponse<WordCompareResponse>>>()
            )
        } throws RuntimeException("API is down")

        // when & then
        val exception = assertThrows(OrderAgentException::class.java) {
            comparatorManager.wordCompare(targetWord, candidates)
        }

        assertEquals(ErrorCode.ORDER_WORD_COMPARE_FAIL, exception.errorCode)
    }
}