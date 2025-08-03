package com.orderagentservice.order

import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.service.UtgDataService
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.neo4j.core.Neo4jClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class UtgDataServiceTest @Autowired constructor(
    private val utgDataService: UtgDataService,
    private val neo4jClient: Neo4jClient
) {
    private val testKioskId = "TEST-MOODTRBL"

    @BeforeEach
    fun setUp() {
        val delQuery = """
            MATCH (n:UI {kioskId: '$testKioskId'}) DETACH DELETE n
        """.trimIndent()
        neo4jClient.query(delQuery)
            .run()

        val createNodesQuery = """
            CREATE
            (category1:UI {id: '면', is_next: true, x: 0, y: 0, title: '면', kioskId: '${testKioskId}'}),
            (category2:UI {id: '사이드', is_next: true, x: 100, y: 100, title: '사이드', kioskId: '${testKioskId}'}),
            (category3:UI {id: '음료', is_next: true, x: 200, y: 200, title: '음료', kioskId: '${testKioskId}'}),
        
            (menu1:UI {id: '시오라멘', is_next: false, x: 300, y: 300, title: '시오라멘', kioskId: '${testKioskId}'}),
            (menu2:UI {id: '쇼유라멘', is_next: false, x: 400, y: 400, title: '쇼유라멘', kioskId: '${testKioskId}'}),
            (menu3:UI {id: '교자', is_next: false, x: 300, y: 300, title: '교자', kioskId: '${testKioskId}'}),
            (menu4:UI {id: '가라아케', is_next: false, x: 400, y: 400, title: '가라아케', kioskId: '${testKioskId}'}),
            (menu5:UI {id: '콜라', is_next: false, x: 300, y: 300, title: '콜라', kioskId: '${testKioskId}'}),
            (menu6:UI {id: '사이다', is_next: false, x: 400, y: 400, title: '사이다', kioskId: '${testKioskId}'}),
        
            (opt1:UI {id: '면추가1', is_next: false, x: 300, y: 300, title: '면추가1', kioskId: '${testKioskId}'}),
            (opt2:UI {id: '면추가2', is_next: false, x: 400, y: 400, title: '면추가2', kioskId: '${testKioskId}'}),
            (opt3:UI {id: '제로1', is_next: false, x: 300, y: 300, title: '제로1', kioskId: '${testKioskId}'}),
            (opt4:UI {id: '제로2', is_next: false, x: 400, y: 400, title: '제로2', kioskId: '${testKioskId}'}),
        
            (back1:UI {id: '선택완료1', is_next: false, x: 300, y: 300, title: '선택완료1', kioskId: '${testKioskId}'}),
            (back2:UI {id: '선택완료2', is_next: false, x: 400, y: 400, title: '선택완료2', kioskId: '${testKioskId}'}),
            (back3:UI {id: '선택완료3', is_next: false, x: 300, y: 300, title: '선택완료3', kioskId: '${testKioskId}'}),
            (back4:UI {id: '선택완료4', is_next: false, x: 400, y: 400, title: '선택완료4', kioskId: '${testKioskId}'})
        """.trimIndent()

        val createRelQuery = """
            MATCH
            (category1:UI {id: '면', kioskId: '${testKioskId}'}),
            (category2:UI {id: '사이드', kioskId: '${testKioskId}'}),
            (category3:UI {id: '음료', kioskId: '${testKioskId}'}),
            (menu1:UI {id: '시오라멘', kioskId: '${testKioskId}'}),
            (menu2:UI {id: '쇼유라멘', kioskId: '${testKioskId}'}),
            (menu3:UI {id: '교자', kioskId: '${testKioskId}'}),
            (menu4:UI {id: '가라아케', kioskId: '${testKioskId}'}),
            (menu5:UI {id: '콜라', kioskId: '${testKioskId}'}),
            (menu6:UI {id: '사이다', kioskId: '${testKioskId}'}),
            (opt1:UI {id: '면추가1', kioskId: '${testKioskId}'}),
            (opt2:UI {id: '면추가2', kioskId: '${testKioskId}'}),
            (opt3:UI {id: '제로1', kioskId: '${testKioskId}'}),
            (opt4:UI {id: '제로2', kioskId: '${testKioskId}'}),
            (back1:UI {id: '선택완료1', kioskId: '${testKioskId}'}),
            (back2:UI {id: '선택완료2', kioskId: '${testKioskId}'}),
            (back3:UI {id: '선택완료3', kioskId: '${testKioskId}'}),
            (back4:UI {id: '선택완료4', kioskId: '${testKioskId}'})
            CREATE
            (category1)-[:HAS_TO]->(menu1), (category1)-[:HAS_TO]->(menu2),
            (category2)-[:HAS_TO]->(menu3), (category2)-[:HAS_TO]->(menu4),
            (category3)-[:HAS_TO]->(menu5), (category3)-[:HAS_TO]->(menu6),
        
            (menu1)-[:OPT_TO]->(opt1), (menu2)-[:OPT_TO]->(opt2),
            (menu5)-[:OPT_TO]->(opt3), (menu6)-[:OPT_TO]->(opt4),
        
            (menu1)-[:BACK_TO]->(back1), (back1)-[:BACK_TO]->(category1),
            (menu2)-[:BACK_TO]->(back2), (back2)-[:BACK_TO]->(category1),
            (menu5)-[:BACK_TO]->(back3), (back3)-[:BACK_TO]->(category3),
            (menu6)-[:BACK_TO]->(back4), (back4)-[:BACK_TO]->(category3),
        
            (category1)-[:PATH_TO]->(category2), (category2)-[:PATH_TO]->(category1),
            (category2)-[:PATH_TO]->(category3), (category3)-[:PATH_TO]->(category1),
            (category3)-[:PATH_TO]->(category1), (category1)-[:PATH_TO]->(category3)
        """.trimIndent()

        neo4jClient.query(createNodesQuery).run()
        neo4jClient.query(createRelQuery).run()
    }

    @Test
    fun `neo4j에 노드를 저장한다`() {
        //then: UI 노드가 주어진다
        val uiDto = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = "url",
            title = "root"
        )

        //when: UI 노드를 저장한다.
        utgDataService.saveNode(uiDto)

        //then: 저장이 완료된다.
    }

    @Test
    fun `neo4j에 경로를 저장한다`() {
        //then: UI 노드가 주어진다
        val uiDto1 = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = "url",
            title = "root1"
        )
        val uiDto2 = UiDto(
            isNext = true,
            x = -1, y = -1,
            kioskId = "url",
            title = "root2"
        )

        //when: UI 노드를 저장하고 관계를 맺는다
        val entity1 = utgDataService.saveNode(uiDto1)
        val entity2 = utgDataService.saveNode(uiDto2)
        utgDataService.saveRel(entity1.id, entity2.id, NodeRelation.PATH_TO)

        //then: 저장이 완료된다
    }

    @Test
    fun `저장된 노드에서 특정 노드까지 경로를 찾는다`() {
        //when: 경로를 찾는다
        val path = utgDataService.findMenuPath(testKioskId, "면", "콜라")

        //then: 올바른 경로가 반환된다
        assertThat(path[0].title).isEqualTo("음료")
        assertThat(path[1].title).isEqualTo("콜라")
    }

    @Test
    fun `현재 메뉴에서 옵션을 찾는다`() {
        //when: 옵션을 찾는다
        val opt1 = utgDataService.findOption(testKioskId, "시오라멘", "면추가1")
        val opt2 = utgDataService.findOption(testKioskId, "쇼유라멘", "면추가2")
        val opt3 = utgDataService.findOption(testKioskId, "사이다", "제로2")

        //then: 옵션이 올바르게 반환된다
        assertThat(opt1.title).isEqualTo("면추가1")
        assertThat(opt2.title).isEqualTo("면추가2")
        assertThat(opt3.title).isEqualTo("제로2")
    }

    @Test
    fun `현재 노드에서 되돌아가는 경로를 찾는다`() {
        //when: 돌아가는 경로를 찾는다
        val backPath = utgDataService.findBackPath(testKioskId, "콜라")

        //then: 경로가 올바르게 반환된다
        assertThat(backPath[0].title).isEqualTo("선택완료3")
        assertThat(backPath[1].title).isEqualTo("음료")
    }

    @Test
    fun `메뉴가 속해있는 카테고리 노드를 찾는다`() {
        //when: 자신의 카테고리를 찾는다
        val categoryNodeId = utgDataService.findCategoryNodeId(testKioskId, "가라아케")

        //then: 올바른 카테고리가 반환된다
        assertThat(categoryNodeId).isEqualTo("사이드")
    }
}