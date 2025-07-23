package com.orderagentservice.order

import com.orderagentservice.order.util.UiExtractorManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class UiExtractorManagerTest @Autowired constructor(
   private val uiExtractorManager: UiExtractorManager
) {
    @Test
    fun `uiExtractorService에게서 이미지 파싱 결과를 가져온다`() {
        //given: 이미지를 가져옴
        val imagePath = "C:\\Users\\hachi\\IdeaProjects\\OrderAgentService\\src\\test\\resources\\opt.png"
        val imageFile = File(imagePath)

        //when: ui extractor에게 이미지 파싱을 요청한다
        val response = uiExtractorManager.getUiComponents(imageFile, "moodTRBL")

        //then: 파싱이 완료된다.
        for (ele in response) {
            println(ele)
        }
    }
}