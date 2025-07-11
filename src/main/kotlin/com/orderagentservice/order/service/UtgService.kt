package com.orderagentservice.order.service

import com.orderagentservice.order.exception.NodeNotFoundException
import com.orderagentservice.order.exception.PathNotFoundException
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.repository.UtgRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtgService @Autowired constructor(
    private val utgRepository: UtgRepository
) {
    @Transactional
    fun saveNode(uiDto: UiDto): UiEntity {
        val uiEntity = utgRepository.save(uiDto.toEntity())
        return uiEntity
    }

    @Transactional
    fun saveRel(sourceId: String, targetId: String, type: NodeRelation) {
        when(type) {
            NodeRelation.PATH_TO -> utgRepository.savePathRelation(sourceId, targetId)
            NodeRelation.HAS_TO -> utgRepository.saveHasRelation(sourceId, targetId)
            NodeRelation.OPT_TO -> utgRepository.saveOptRelation(sourceId, targetId)
            NodeRelation.BACK_TO -> utgRepository.saveBackRelation(sourceId, targetId)
            else -> NodeRelation.NONE
        }
    }

    fun findMenuPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto> {
        val nodesList = utgRepository.findPathByTitle(kioskId, sourceId, targetTitle)
            .ifEmpty { throw PathNotFoundException() }

        return nodesList
            .map { node -> ActionPathDto(
                node["id"].toString(), node["title"].toString(),
                x = (node["x"] as Number).toInt(), y = (node["y"] as Number).toInt()
            ) }
            .drop(1)
    }

    fun findOptionNode(kioskId: String, menuId: String, optKeyword: String): ActionPathDto {
        val entity = utgRepository.findOptionByTitle(kioskId, menuId, optKeyword)
            ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        val nodesList = utgRepository.findBackPathByTitle(kioskId, sourceId)
            .ifEmpty { throw PathNotFoundException() }

        return nodesList
            .map { node -> ActionPathDto(
                node["id"].toString(), node["title"].toString(),
                x = (node["x"] as Number).toInt(), y = (node["y"] as Number).toInt()
            ) }
            .drop(1)
    }

    fun findCategoryNodeId(kioskId: String, id: String): String {
        val entity = utgRepository.findIncomingHasTo(kioskId, id)
            ?: throw NodeNotFoundException()

        return entity.id
    }

    fun findPlaceNodeId(kioskId: String, id: String, place: String): ActionPathDto? {
        val entity = utgRepository.findPlaceByTitle(kioskId, id, place) ?: return null

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }
}