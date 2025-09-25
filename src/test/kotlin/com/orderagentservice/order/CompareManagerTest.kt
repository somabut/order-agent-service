package com.orderagentservice.order

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.service.utg.ComparatorManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
class CompareManagerTest @Autowired constructor(
    private val compareManager: ComparatorManager
) {
    @Test
    fun `이미지 비교를 수행한다`() {
        val sourcePath = "/Users/moodtrbl/IdeaProjects/order-agent-service/src/test/resources/main.png"
        val sourceFile = File(sourcePath)
        val source = sourceFile.readBytes()

        val targePath = "/Users/moodtrbl/IdeaProjects/order-agent-service/src/test/resources/side_menu.png"
        val targetFile = File(targePath)
        val target = targetFile.readBytes()

        val type = "image/png"

        val result = compareManager.imageCompare(source, type, target, type)
        println(result)
    }

    @Test
    fun `단어 비교를 수행한다`() {
        val target = "라민"
        val candidates = listOf(
            UiComponentDto(x=237, y=88, minX=237, minY=88, maxX=353, maxY=165, title="라면"),
            UiComponentDto(x = 12, y = 421, minX = 12, minY = 421, maxX = 123, maxY = 493, title = "피자"),
            UiComponentDto(x=410, y=77, minX=410, minY=77, maxX=516, maxY=142, title="치킨"),
            UiComponentDto(x=98, y=250, minX=98, minY=250, maxX=198, maxY=310, title="초밥"),
            UiComponentDto(x=320, y=330, minX=320, minY=330, maxX=425, maxY=400, title="샌드위치"),
        )

        val wordDto = compareManager.wordCompare(target, candidates)
        println(wordDto)
    }
}