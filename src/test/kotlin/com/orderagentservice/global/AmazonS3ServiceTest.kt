package com.orderagentservice.global

import com.orderagentservice.global.service.AmazonS3Service
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class AmazonS3ServiceTest @Autowired constructor(
    private val amazonS3Service: AmazonS3Service
) {
    @Test
    fun `이미지를 s3에 저장한다`() {
        val kioskId = "moodtrbl"
        val commandId = "command"
        val file = File("C:\\Users\\hachi\\IdeaProjects\\OrderAgentService\\src\\test\\resources\\backs.png")
        amazonS3Service.saveFile(kioskId, commandId, byteArrayOf(), "")
    }
}