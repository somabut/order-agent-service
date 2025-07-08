package com.orderagentservice.order.service

import com.orderagentservice.order.exception.NodeNotFoundException
import com.orderagentservice.order.exception.PathNotFoundException
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.repository.UiRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UiGraphService @Autowired constructor(
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

    fun findTargetPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto> {
        val nodesList = uiRepository.findPathByTitle(kioskId, sourceId, targetTitle)
            .ifEmpty { throw PathNotFoundException() }

        return nodesList
            .map { node -> ActionPathDto(
                node["id"].toString(), node["title"].toString(),
                x = (node["x"] as Number).toInt(), y = (node["y"] as Number).toInt()
            ) }
            .drop(1)
    }

    fun findOptTarget(kioskId: String, menuId: String, optKeyword: String): ActionPathDto {
        val entity = uiRepository.findOptionByTitle(kioskId, menuId, optKeyword)
            ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        val nodesList = uiRepository.findBackPathByTitle(kioskId, sourceId)
            .ifEmpty { throw PathNotFoundException() }

        return nodesList
            .map { node -> ActionPathDto(
                node["id"].toString(), node["title"].toString(),
                x = (node["x"] as Number).toInt(), y = (node["y"] as Number).toInt()
            ) }
            .drop(1)
    }

    fun findCategoryNodeId(kioskId: String, id: String): String {
        val entity = uiRepository.findIncomingHasTo(kioskId, id)
            ?: throw NodeNotFoundException()

        return entity.id
    }
}