package com.orderagentservice.global.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.orderagentservice.global.exception.S3NotSupportedType
import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.util.ImageUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class AmazonS3Service @Autowired constructor(
    private val amazonS3: AmazonS3,
    private val env: Environment
) {
    private val bucket: String = env.getProperty("cloud.aws.credentials.s3.bucket")!!

    private val allowTypes = listOf("image/png", "image/jpeg", "image/jpg")

    fun saveFile(kioskId:String, commandId: String, fileBytes: ByteArray, contentType: String): String {
        val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formattedDate = now.format(dayFormatter)
        val formattedTime = now.format(timeFormatter)

        val objectMetadata = ObjectMetadata()
        if (contentType !in allowTypes) {
            throw S3NotSupportedType()
        }

        objectMetadata.contentLength = fileBytes.size.toLong()
        objectMetadata.contentType = contentType
        val extension = ImageUtils.getExtension(contentType)

        val fileName = getImagePath(
            kioskId = kioskId, commandId = commandId,
            formattedDate = formattedDate, formattedTime = formattedTime,
            extension = extension
        )

        fileBytes.inputStream().use { inputStream ->
            amazonS3.putObject(
                PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
            )
        }

        return getImagePath(kioskId, commandId, formattedDate, formattedTime, extension)
    }

    private fun getImagePath(kioskId: String, commandId: String, formattedDate: String, formattedTime: String, extension: String)
        = "image/${kioskId}/${formattedDate}/${getImageName(commandId = commandId, formattedTime = formattedTime)}.${extension}"

    private fun getImageName(commandId: String, formattedTime: String)
        = "${formattedTime}_${commandId}"
}