package com.orderagentservice.unit.global.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.PutObjectResult
import com.orderagentservice.global.exception.S3NotSupportedType
import com.orderagentservice.global.service.AmazonS3Service
import com.orderagentservice.global.util.ImageUtils
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.env.Environment
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
class AmazonS3ServiceTest {
    @MockK
    private lateinit var amazonS3: AmazonS3

    @MockK
    private lateinit var env: Environment

    private lateinit var amazonS3Service: AmazonS3Service

    private val MOCK_BUCKET = "test-bucket"
    private val AWS_S3_URL = "https://alike-image.s3.ap-southeast-2.amazonaws.com"
    private val MOCK_KIOSK_ID = "kiosk-123"
    private val MOCK_COMMAND_ID = "cmd-test"
    private val MOCK_FILE_BYTES = byteArrayOf(1, 2, 3, 4, 5)

    private val MOCK_SUPPORTED_TYPES = listOf("image/png", "image/jpeg", "image/jpg")
    private val MOCK_EXPECTED_EXTENSIONS = listOf("png", "jpg", "jpg")

    private val MOCK_UNSUPPORTED_TYPES = listOf("image/gif", "application/pdf", "", "text/plain")

    private val MOCK_NOW = LocalDateTime.of(2003, 3, 1, 11, 11, 11)
    private val EXPECTED_DATE = "2003-03-01"
    private val EXPECTED_TIME = "11-11-11"

    @BeforeEach
    fun setUp() {
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now(ZoneId.of("Asia/Seoul")) } returns MOCK_NOW
        mockkStatic(ImageUtils.Companion::class)

        every { env.getProperty("cloud.aws.credentials.s3.bucket") } returns MOCK_BUCKET

        amazonS3Service = AmazonS3Service(amazonS3, env)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(LocalDateTime::class)
        unmockkStatic(ImageUtils.Companion::class)
    }

    @Test
    @DisplayName("지원하는 파일 형식(png, jpg, jpeg) 저장에 성공하고 올바른 S3 URL을 반환한다")
    fun `saveFile should upload file and return correct URL for supported types`() {
        // given
        val testCases = MOCK_SUPPORTED_TYPES.zip(MOCK_EXPECTED_EXTENSIONS)

        val requestSlot = slot<PutObjectRequest>()
        every { amazonS3.putObject(capture(requestSlot)) } returns mockk()

        // when & then
        testCases.forEach { (contentType, extension) ->
            // given
            val expectedFileName = "image/$MOCK_KIOSK_ID/$EXPECTED_DATE/${EXPECTED_TIME}_$MOCK_COMMAND_ID.$extension"
            val expectedUrl = "$AWS_S3_URL/$expectedFileName"

            // when
            val actualUrl = amazonS3Service.saveFile(
                kioskId = MOCK_KIOSK_ID,
                commandId = MOCK_COMMAND_ID,
                fileBytes = MOCK_FILE_BYTES,
                contentType = contentType
            )

            // then
            assertThat(actualUrl).isEqualTo(expectedUrl)

            verify { amazonS3.putObject(any<PutObjectRequest>()) }

            val capturedRequest = requestSlot.captured
            assertThat(capturedRequest.bucketName).isEqualTo(MOCK_BUCKET)
            assertThat(capturedRequest.key).isEqualTo(expectedFileName)

            val metadata = capturedRequest.metadata
            assertThat(metadata.contentLength).isEqualTo(MOCK_FILE_BYTES.size.toLong())
            assertThat(metadata.contentType).isEqualTo(contentType)
        }
    }

    @Test
    @DisplayName("지원하지 않는 파일 형식(gif, pdf 등)의 경우 S3NotSupportedType 예외를 던진다")
    fun `saveFile should throw S3NotSupportedType for unsupported types`() {
        // when & then
        MOCK_UNSUPPORTED_TYPES.forEach { unsupportedType ->
            // when
            assertThrows<S3NotSupportedType> {
                amazonS3Service.saveFile(
                    kioskId = MOCK_KIOSK_ID,
                    commandId = MOCK_COMMAND_ID,
                    fileBytes = MOCK_FILE_BYTES,
                    contentType = unsupportedType
                )
            }
        }

        // then
        verify(exactly = 0) { amazonS3.putObject(any()) }
    }
}