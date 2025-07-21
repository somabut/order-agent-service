package com.orderagentservice.global

import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.service.UtgService
import com.orderagentservice.order.util.ImageUtils
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class LearningTest @Autowired constructor(
    private val globalLogger: GlobalLogger,
    private val utgService: UtgService,
) {
    @Test
    fun `asdfas`() {
        val imagePath = "C:\\Users\\hachi\\IdeaProjects\\OrderAgentService\\src\\test\\resources\\main.png"
        val imageFile = File(imagePath)

        println(ImageUtils.imageToHash(imageFile))
        println(ImageUtils.imageToHash(imageFile))
        println(ImageUtils.imageToHash(imageFile))
    }
}