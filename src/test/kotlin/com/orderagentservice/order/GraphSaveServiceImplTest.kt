package com.orderagentservice.order

import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.neo4j.core.Neo4jClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class GraphSaveServiceImplTest @Autowired constructor(
    private val graphService: UiGraphService,
    private val neo4jClient: Neo4jClient
) {
    private val testKioskId = "TEST-MOODTRBL"

    @Test
    fun `neo4j에 노드를 저장한다`() {
        //then: UI 노드가 주어진다
        val uiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = "url",
            title = "root",
            type = NodeType.ROOT
        )

        //when: UI 노드를 저장한다.
        graphService.saveNode(uiDto)

        //then: 저장이 완료된다.
    }

    @Test
    fun `neo4j에 경로를 저장한다`() {
        //then: UI 노드가 주어진다
        val uiDto1 = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = "url",
            title = "root1",
            type = NodeType.ROOT
        )
        val uiDto2 = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = "url",
            title = "root2",
            type = NodeType.ROOT
        )

        //when: UI 노드를 저장하고 관계를 맺는다
        val entity1 = graphService.saveNode(uiDto1)
        val entity2 = graphService.saveNode(uiDto2)
        graphService.saveRel(entity1.id, entity2.id, NodeRelationType.PATH_TO)

        //then: 저장이 완료된다
    }

    @Test
    fun `저장된 노드에서 특정 노드까지 경로를 찾는다`() {
        //when: 경로를 찾는다
        val path = graphService.findPath(testKioskId, "면", "콜라")

        //then: 올바른 경로가 반환된다
        assertThat(path[0].title).isEqualTo("음료")
        assertThat(path[1].title).isEqualTo("콜라")
    }

    @Test
    fun `현재 메뉴에서 옵션을 찾는다`() {
        //when: 옵션을 찾는다
        val opt1 = graphService.findOption(testKioskId, "시오라멘", "면추가1")
        val opt2 = graphService.findOption(testKioskId, "쇼유라멘", "면추가2")
        val opt3 = graphService.findOption(testKioskId, "사이다", "제로2")

        //then: 옵션이 올바르게 반환된다
        assertThat(opt1.title).isEqualTo("면추가1")
        assertThat(opt2.title).isEqualTo("면추가2")
        assertThat(opt3.title).isEqualTo("제로2")
    }

    @Test
    fun `현재 노드에서 되돌아가는 경로를 찾는다`() {
        //when: 돌아가는 경로를 찾는다
        val backPath = graphService.findBackPath(testKioskId, "콜라")

        //then: 경로가 올바르게 반환된다
        assertThat(backPath[0].title).isEqualTo("선택완료3")
        assertThat(backPath[1].title).isEqualTo("음료")
    }

    @Test
    fun `메뉴가 속해있는 카테고리 노드를 찾는다`() {
        //when: 자신의 카테고리를 찾는다
        val categoryNodeId = graphService.findCategoryNodeId(testKioskId, "가라아케")

        //then: 올바른 카테고리가 반환된다
        assertThat(categoryNodeId).isEqualTo("사이드")
    }

    @Test
    fun `모든 노드를 가져온다`() {
        val kioskId = "kiosk-2303452c-8454-4b9f-add2-47314cfd3911"
        val nodes = graphService.findAll(kioskId)
        for (node in nodes) println(node)
    }

    @Test
    fun `변경된 노드를 가져온다`() {
        val kioskId = "kiosk-2303452c-8454-4b9f-add2-47314cfd3911"
        val nodes = graphService.findModified(kioskId)
            .filter { it.type == NodeType.CATEGORY }
            .map { it.title }

        for (node in nodes) println(node)
    }
}