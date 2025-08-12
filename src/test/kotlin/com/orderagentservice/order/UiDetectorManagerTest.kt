package com.orderagentservice.order

import com.orderagentservice.order.service.utg.UiDetectorManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class UiDetectorManagerTest @Autowired constructor(
   private val uiDetectorManager: UiDetectorManager
) {
    @Test
    fun `uiExtractorService에게서 이미지 파싱 결과를 가져온다`() {
        //given: 이미지를 가져옴
        val imagePath = "C:\\Users\\hachi\\IdeaProjects\\OrderAgentService\\src\\test\\resources\\opt.png"
        val imageFile = File(imagePath)

        //when: ui extractor에게 이미지 파싱을 요청한다
//        val response = uiExtractorManager.getUiComponents("moodTRBL")
        val response = uiDetectorManager.queryUiExtractor(imageFile, "ocr")

        //then: 파싱이 완료된다.
        for (ele in response) {
            println(ele)
        }
    }
}