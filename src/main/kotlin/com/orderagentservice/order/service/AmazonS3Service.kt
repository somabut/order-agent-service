package com.orderagentservice.order.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class AmazonS3Service @Autowired constructor(
    private val amazonS3: AmazonS3,
    private val env: Environment
) {
    private val bucket: String = env.getProperty("cloud.aws.credentials.s3.bucket")!!

    fun saveFile(kioskId:String, multipartFile: MultipartFile) {
        val fileName = getFileName(kioskId)

        val objectMetadata = ObjectMetadata()
        objectMetadata.contentLength = multipartFile.size
        objectMetadata.contentType = multipartFile.contentType
        val inputStream = multipartFile.inputStream

        amazonS3.putObject(
            PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead)
        )
    }

    private fun getFileName(kioskId: String): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        val formattedTime = now.format(formatter)
        return "image/${kioskId}_$formattedTime"
    }
}