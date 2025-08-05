package com.orderagentservice.order.service

import com.orderagentservice.logger
import com.orderagentservice.order.exception.NodeNotFoundException
import com.orderagentservice.order.exception.PathNotFoundException
import com.orderagentservice.order.model.NodeRelation
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.repository.GraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GraphService @Autowired constructor(
    private val graphRepository: GraphRepository
) {
    private val log = logger()

    @Transactional
    fun saveNode(uiDto: UiDto): UiEntity {
        log.info("노드 저장. ${uiDto.title}")
        val uiEntity = graphRepository.save(uiDto.toEntity())
        return uiEntity
    }

    @Transactional
    fun saveRel(sourceId: String, targetId: String, type: NodeRelation) {
        log.info("관계 설정. ${sourceId} [${type.name}]-> ${targetId}")
        when(type) {
            NodeRelation.PATH_TO -> graphRepository.savePathRelation(sourceId, targetId)
            NodeRelation.HAS_TO -> graphRepository.saveHasRelation(sourceId, targetId)
            NodeRelation.OPT_TO -> graphRepository.saveOptRelation(sourceId, targetId)
            NodeRelation.BACK_TO -> graphRepository.saveBackRelation(sourceId, targetId)
            else -> NodeRelation.NONE
        }
    }

    fun findMenuPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto> {
        val nodesList = graphRepository.findPathByTitle(kioskId, sourceId, targetTitle)
            .ifEmpty { throw PathNotFoundException() }
            .filter { node -> node["title"].toString() != "station" }
            .drop(1)

        return ActionPathDto.toPathDtoList(nodesList)
    }

    fun findPaymentPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        val nodesList = graphRepository.findPathByTitle(kioskId, sourceId, "complete")
            .ifEmpty { throw PathNotFoundException() }
            .filter { node -> node["title"].toString() != "station" }

        return ActionPathDto.toPathDtoList(nodesList)
    }

    fun findOption(kioskId: String, menuId: String, optKeyword: String): ActionPathDto {
        val entity = graphRepository.findOptionByTitle(kioskId, menuId, optKeyword)
            ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        val nodesList = graphRepository.findBackPathByTitle(kioskId, sourceId)
            .ifEmpty { throw PathNotFoundException() }
            .drop(1)

        return ActionPathDto.toPathDtoList(nodesList)
    }

    fun findCategoryNodeId(kioskId: String, id: String): String {
        val entity = graphRepository.findIncomingHasTo(kioskId, id)
            ?: throw NodeNotFoundException()

        return entity.id
    }

    fun findPlace(kioskId: String, id: String, place: String): ActionPathDto? {
        val entity = graphRepository.findPlaceByTitle(kioskId, id, place) ?: return null

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun findRoot(kioskId: String): ActionPathDto {
        val entity = graphRepository.findRootNode(kioskId) ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun findStation(kioskId: String): ActionPathDto {
        val entity = graphRepository.findStationNode(kioskId) ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    fun deleteMenusByCategory(kioskId: String, id: String) {
        graphRepository.deleteMenuNode(id, kioskId)
    }
}