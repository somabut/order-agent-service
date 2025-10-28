package com.orderagentservice.unit.global.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.orderagentservice.global.util.HttpStatusFlexibleDeserializer
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpStatus
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class HttpStatusFlexibleDeserializerTest {
    @MockK
    private lateinit var parser: JsonParser

    private val context: DeserializationContext = mockk(relaxed = true)

    private lateinit var deserializer: HttpStatusFlexibleDeserializer

    @BeforeEach
    fun setUp() {
        // 테스트할 대상 클래스를 초기화합니다.
        deserializer = HttpStatusFlexibleDeserializer()
    }

    companion object {
        // 유효한 입력값과 기대되는 HttpStatus를 제공하는 static 메소드
        @JvmStatic
        fun validHttpStatusProvider(): Stream<Arguments> = Stream.of(
            Arguments.of("200", HttpStatus.OK),
            Arguments.of("404", HttpStatus.NOT_FOUND),
            Arguments.of("500", HttpStatus.INTERNAL_SERVER_ERROR),
            Arguments.of("201", HttpStatus.CREATED),

            Arguments.of("OK", HttpStatus.OK),
            Arguments.of("NOT_FOUND", HttpStatus.NOT_FOUND),
            Arguments.of("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR),

            Arguments.of("ok", HttpStatus.OK),
            Arguments.of("not_found", HttpStatus.NOT_FOUND),

            Arguments.of("Accepted", HttpStatus.ACCEPTED)
        )
    }

    @ParameterizedTest
    @MethodSource("validHttpStatusProvider")
    @DisplayName("숫자 및 텍스트 형식의 유효한 문자열을 올바른 HttpStatus로 변환한다")
    fun `should deserialize valid numeric and text strings to correct HttpStatus`(
        inputString: String,
        expectedStatus: HttpStatus
    ) {
        // given
        every { parser.text } returns inputString

        // when
        val actualStatus = deserializer.deserialize(parser, context)

        // then
        assertThat(actualStatus).isEqualTo(expectedStatus)
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["", "   ", "999", "NOT_A_REAL_STATUS", "THIS_IS_INVALID"])
    @DisplayName("null, 공백, 알 수 없는 코드나 텍스트는 null을 반환한다")
    fun `should return null for invalid, unknown, or null strings`(inputString: String?) {
        // given
        every { parser.text } returns inputString

        // when
        val actualStatus = deserializer.deserialize(parser, context)

        // then
        assertThat(actualStatus).isNull()
    }
}