package com.orderagentservice.order

import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.repository.GraphRepository
import com.orderagentservice.order.service.graph.GraphService
import com.orderagentservice.order.service.graph.GraphServiceImpl
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.neo4j.core.Neo4jClient
import kotlin.contracts.contract

@SpringBootTest
class GraphRepositoryTest @Autowired constructor(
    private val graphService: GraphService,
    private val graphRepository: GraphRepository,
    private val neo4jClient: Neo4jClient
) {
    @Test
    fun `원하는 메뉴의 경로를 찾는다`() {
//        val kioskId = "kiosk-d89e07fa-4361-4b6a-a550-ac580a1ba195"
        val kioskId = "kiosk-8f22eeb6-a920-44ed-af26-d800756fb283"
        val nowNode = graphService.findRoot(kioskId)

        println(nowNode)
//        val menuId = "b85ef30a-7558-4480-aea7-a9b8b41f7c55"
        val menuId = "fcdd70d8-021f-4671-bf91-21bafff17557"

//        val path = graphService.findPath(kioskId, menuId, "station")
        val path = graphService.findStation(kioskId)
        println(path)
    }

    @Test
    fun `노드를 저장한다`() {
        val kioskId = "test"
        val entity = graphService.saveNode(
            UiDto(
                isNext = false,
                x = -1, y = -1,
                title = "test", kioskId = kioskId
            )
        )
        println(entity)
    }

    @Test
    fun `테스트용 노드 추가`() {
        val result = neo4jClient.query("""
            MATCH (n: UI {kioskId: 'kiosk-d89e07fa-4361-4b6a-a550-ac580a1ba195'})-[r]->(m)
            RETURN n, r, m
        """)
            .fetch()
            .all()

        result.forEachIndexed  { idx, row ->
            val n = row["n"] as org.neo4j.driver.types.Node
            val r = row["r"] as org.neo4j.driver.types.Relationship
            val m = row["m"] as org.neo4j.driver.types.Node

            println("${n.asMap()} ${r.type()} ${m.asMap()}")

            val nMap = n.asMap().toMutableMap().apply { set("kioskId", "GRAPH_TEST") }
            val mMap = m.asMap().toMutableMap().apply { set("kioskId", "GRAPH_TEST") }

            fun propStr(label: String, data: Map<String, Any?>): String = data.filterKeys { it != "id" && it != "kioskId" }.entries.joinToString(", ") {
                val v = when (it.key) {
                    "created_at" -> "datetime(\"${it.value}\")"
                    "title" -> "\"${it.value}\""
                    "is_next" -> it.value.toString()
                    else -> it.value.toString()
                }
                "$label.${it.key} = $v"
            }

            val nLabel = "n$idx"
            val mLabel = "m$idx"
            val relType = r.type()

            val query = """
                MERGE ($nLabel:UI {id: "${nMap["id"]}", kioskId: "GRAPH_TEST"})
                  ON CREATE SET ${propStr(nLabel, nMap)}
                MERGE ($mLabel:UI {id: "${mMap["id"]}", kioskId: "GRAPH_TEST"})
                  ON CREATE SET ${propStr(mLabel, mMap)}
                MERGE ($nLabel)-[:$relType]->($mLabel)
            """.trimIndent()

            neo4jClient.query(query).run()
        }
    }

    @Test
    fun `노드 삭제`() {
        val kioskId = "GRAPH_TEST"
        graphService.deleteMenusByCategory(kioskId, "34785735-8acf-4d35-b04d-4788587738a0")
    }

    @Test
    fun `활성화 프로필에 맞게 서비스를 로드한다`() {
        val dto = UiDto(
            isNext = false,
            x = -1, y = -1,
            title = "title", kioskId = "id"
        )
        val result = graphService.saveNode(dto)
//        Assertions.assertThat(result.kioskId).isEqualTo("TEST")
        Assertions.assertThat(result.kioskId).isNotEqualTo("TEST")
    }
}
