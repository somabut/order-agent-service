package com.orderagentservice.order.service

import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.repository.UiRepository
import org.neo4j.driver.internal.InternalPath
import org.neo4j.driver.types.Path
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UiGraphService @Autowired constructor(
    private val neo4jClient: Neo4jClient,
    private val uiRepository: UiRepository
) {
    @Transactional
    fun saveNode(uiDto: UiDto): UiEntity {
        val uiEntity = uiRepository.save(uiDto.toEntity())
        return uiEntity
    }

    @Transactional
    fun saveRel(sourceId: String, targetId: String, type: NodeRelation) {
        when(type) {
            NodeRelation.PATH_TO -> uiRepository.savePathRelation(sourceId, targetId)
            NodeRelation.HAS_TO -> uiRepository.saveHasRelation(sourceId, targetId)
            NodeRelation.OPT_TO -> uiRepository.saveOptRelation(sourceId, targetId)
            NodeRelation.BACK_TO -> uiRepository.saveBackRelation(sourceId, targetId)
            else -> NodeRelation.NONE
        }
    }

    fun findTargetPath(url: String, target: String, rootTitle: String = "root"): List<Pair<Int, Int>> {
        val paths = uiRepository.findPathByTitle(url, target, rootTitle)
        val targetPath = paths.flatMap { path ->
            path.nodes().map { node ->
                Pair(node["x"].asInt(), node["y"].asInt())
            }
        }.drop(1)

        return targetPath
    }
}