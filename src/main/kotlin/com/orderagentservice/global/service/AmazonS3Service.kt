package com.orderagentservice.global.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.orderagentservice.global.exception.S3NotSupportedType
import com.orderagentservice.global.model.ErrorCode
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

    fun saveFile(kioskId:String, commandId: String, file: File): String {
        val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formattedDate = now.format(dayFormatter)
        val formattedTime = now.format(timeFormatter)

        val objectMetadata = ObjectMetadata()
        val contentType = Files.probeContentType(file.toPath())
        if (contentType !in allowTypes) {
            throw S3NotSupportedType()
        }

        objectMetadata.contentLength = file.length()
        objectMetadata.contentType = contentType
        val extension = getExtension(contentType)

        val fileName = getImagePath(
            kioskId = kioskId, commandId = commandId,
            formattedDate = formattedDate, formattedTime = formattedTime,
            extension = extension
        )

        val fileBytes = file.readBytes()
        fileBytes.inputStream().use { inputStream ->
            amazonS3.putObject(
                PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
            )
        }

        return getImageName(commandId = commandId, formattedTime = formattedTime)
    }

    private fun getImagePath(kioskId: String, commandId: String, formattedDate: String, formattedTime: String, extension: String)
        = "image/${kioskId}/${formattedDate}/${getImageName(commandId = commandId, formattedTime = formattedTime)}.${extension}"

    private fun getImageName(commandId: String, formattedTime: String)
        = "${formattedTime}_${commandId}"

    private fun getExtension(contentType: String) =
        when(contentType) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            else -> throw S3NotSupportedType()
        }

}