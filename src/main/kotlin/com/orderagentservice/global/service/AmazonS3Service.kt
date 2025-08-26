package com.orderagentservice.global.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class AmazonS3Service @Autowired constructor(
    private val amazonS3: AmazonS3,
    private val env: Environment
) {
    private val bucket: String = env.getProperty("cloud.aws.credentials.s3.bucket")!!

    fun saveFile(kioskId:String, commandId: String, file: File): String {
        val now = LocalDateTime.now()
        val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formattedDate = now.format(dayFormatter)
        val formattedTime = now.format(timeFormatter)

        val fileName = getImagePath(
            kioskId = kioskId, commandId = commandId,
            formattedDate = formattedDate, formattedTime = formattedTime
        )

        val objectMetadata = ObjectMetadata()
        val contentType = Files.probeContentType(file.toPath())
        if (contentType != "image/png") {
            throw IllegalArgumentException("Only PNG files are allowed")
        }

        objectMetadata.contentLength = file.length()
        objectMetadata.contentType = contentType

        file.inputStream().use { inputStream ->
            amazonS3.putObject(
                PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
            )
        }

        return getImageName(commandId = commandId, formattedTime = formattedTime)
    }

    private fun getImagePath(kioskId: String, commandId: String, formattedDate: String, formattedTime: String)
        = "image/${kioskId}/${formattedDate}/${getImageName(commandId = commandId, formattedTime = formattedTime)}.png"

    private fun getImageName(commandId: String, formattedTime: String)
        = "${formattedTime}_${commandId}"
}