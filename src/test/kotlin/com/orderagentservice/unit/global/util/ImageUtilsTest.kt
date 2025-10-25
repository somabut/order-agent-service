package com.orderagentservice.unit.global.util

import com.orderagentservice.global.exception.S3NotSupportedType
import com.orderagentservice.global.util.ImageUtils
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(MockKExtension::class)
class ImageUtilsTest {
    private val MOCK_TYPE = listOf("image/png", "image/jpeg", "image/jpg")
    private val MOCK_EXTENSION = listOf("png", "jpg", "jpg")

    @Test
    @DisplayName("지원하는 Mime-Type을 올바른 확장자로 변환한다")
    fun `should return correct extension for supported types`() {
        val testCases = MOCK_TYPE.zip(MOCK_EXTENSION)

        // when & then
        testCases.forEach { (contentType, expectedExtension) ->
            // when
            val actualExtension = ImageUtils.getExtension(contentType)

            // then
            assertThat(actualExtension).isEqualTo(expectedExtension)
        }
    }

    @Test
    @DisplayName("지원하지 않는 Mime-Type(gif)의 경우 S3NotSupportedType 예외를 던진다")
    fun `should throw exception for unsupported type 'image-gif'`() {
        // given
        val unsupportedType = "image/gif"

        // when & then
        assertThrows<S3NotSupportedType> { ImageUtils.getExtension(unsupportedType) }
    }

    @Test
    @DisplayName("빈 문자열 Mime-Type의 경우 S3NotSupportedType 예외를 던진다")
    fun `should throw exception for empty string`() {
        // given
        val emptyType = ""

        // when & then
        assertThrows<S3NotSupportedType> { ImageUtils.getExtension(emptyType) }
    }
}