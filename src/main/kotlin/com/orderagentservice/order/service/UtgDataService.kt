package com.orderagentservice.order.service

import com.orderagentservice.logger
import com.orderagentservice.order.exception.NodeNotFoundException
import com.orderagentservice.order.exception.PathNotFoundException
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.repository.UtgDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtgDataService @Autowired constructor(
    private val utgDataRepository: UtgDataRepository
) {
    private val log = logger()

    @Transactional
    fun saveNode(uiDto: UiDto): UiEntity {
        log.info("노드 저장. ${uiDto.title}")
        val uiEntity = utgDataRepository.save(uiDto.toEntity())
        return uiEntity
    }

    @Transactional
    fun saveRel(sourceId: String, targetId: String, type: NodeRelation) {
        log.info("관계 설정. ${sourceId} [${type.name}]-> ${targetId}")
        when(type) {
            NodeRelation.PATH_TO -> utgDataRepository.savePathRelation(sourceId, targetId)
            NodeRelation.HAS_TO -> utgDataRepository.saveHasRelation(sourceId, targetId)
            NodeRelation.OPT_TO -> utgDataRepository.saveOptRelation(sourceId, targetId)
            NodeRelation.BACK_TO -> utgDataRepository.saveBackRelation(sourceId, targetId)
            else -> NodeRelation.NONE
        }
    }

    fun findMenuPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto> {
        val nodesList = utgDataRepository.findPathByTitle(kioskId, sourceId, targetTitle)
            .ifEmpty { throw PathNotFoundException() }

        return nodesList
            .filter { node -> node["title"].toString() != "station" }
            .map { node -> ActionPathDto(
                node["id"].toString(), node["title"].toString(),
                x = (node["x"] as Number).toInt(), y = (node["y"] as Number).toInt()
            ) }
            .drop(1)
    }

    fun findOption(kioskId: String, menuId: String, optKeyword: String): ActionPathDto {
        val entity = utgDataRepository.findOptionByTitle(kioskId, menuId, optKeyword)
            ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        val nodesList = utgDataRepository.findBackPathByTitle(kioskId, sourceId)
            .ifEmpty { throw PathNotFoundException() }

        return nodesList
            .map { node -> ActionPathDto(
                node["id"].toString(), node["title"].toString(),
                x = (node["x"] as Number).toInt(), y = (node["y"] as Number).toInt()
            ) }
            .drop(1)
    }

    fun findCategoryNodeId(kioskId: String, id: String): String {
        val entity = utgDataRepository.findIncomingHasTo(kioskId, id)
            ?: throw NodeNotFoundException()

        return entity.id
    }

    fun findPlace(kioskId: String, id: String, place: String): ActionPathDto? {
        val entity = utgDataRepository.findPlaceByTitle(kioskId, id, place) ?: return null

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun findRoot(kioskId: String): ActionPathDto {
        val entity = utgDataRepository.findRootNode(kioskId) ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun findStation(kioskId: String): ActionPathDto {
        val entity = utgDataRepository.findStationNode(kioskId) ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun deleteMenusByCategory(kioskId: String, id: String) {
        utgDataRepository.deleteMenuNode(id, kioskId)
    }
}