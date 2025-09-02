package com.orderagentservice.order.service.graph.ui

import com.orderagentservice.logger
import com.orderagentservice.order.exception.NodeNotFoundException
import com.orderagentservice.order.exception.PathNotFoundException
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.model.type.SpecialNodeType
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.repository.ui.UiGraphRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Profile("!test")
@Service
class UiGraphServiceImpl @Autowired constructor(
    private val graphRepository: UiGraphRepository
) : UiGraphService {
    private val log = logger()

    @Transactional
    override fun saveNode(uiDto: UiDto): UiEntity {
        log.info("UI 노드 저장. ${uiDto.title}")
        val uiEntity = graphRepository.save(uiDto.toEntity())
        return uiEntity
    }

    @Transactional
    override fun saveRel(sourceId: String, targetId: String, type: NodeRelationType) {
        log.info("관계 설정. ${sourceId} [${type.name}]-> ${targetId}")
        when(type) {
            NodeRelationType.PATH_TO -> graphRepository.savePathRelation(sourceId, targetId)
            NodeRelationType.HAS_TO -> graphRepository.saveHasRelation(sourceId, targetId)
            NodeRelationType.OPT_TO -> graphRepository.saveOptRelation(sourceId, targetId)
            NodeRelationType.BACK_TO -> graphRepository.saveBackRelation(sourceId, targetId)
            NodeRelationType.MATCH_TO -> graphRepository.saveMathRelation(sourceId, targetId)
            NodeRelationType.IMAGE_TO -> graphRepository.saveImageRelation(sourceId, targetId)
            else -> NodeRelationType.NONE
        }
    }

    override fun findNodeByTitle(kioskId: String, title: String): String {
        val node = graphRepository.findNodeByTitle(kioskId, title) ?: throw NodeNotFoundException()
        return node.id
    }

    override fun findPath(kioskId: String, sourceId: String, targetTitle: String): List<ActionPathDto> {
        val nodesList = graphRepository.findPathByTitle(kioskId, sourceId, targetTitle)
            .ifEmpty { throw PathNotFoundException() }
            .filter { node ->
                val title = node["title"].toString()
                title != SpecialNodeType.STATION.title && !title.startsWith("menu:")
            }
            .drop(1)

        return ActionPathDto.toPathDtoList(nodesList)
    }

    override fun findPaymentPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        val nodesList = graphRepository.findPathByTitle(kioskId, sourceId,  SpecialNodeType.COMPLETE.title)
            .ifEmpty { throw PathNotFoundException() }
            .filter { node -> node["title"].toString() != SpecialNodeType.STATION.title }

        return ActionPathDto.toPathDtoList(nodesList)
    }

    override fun findOption(kioskId: String, menuId: String, optKeyword: String): ActionPathDto {
        val entity = graphRepository.findOptionByTitle(kioskId, menuId, optKeyword)
            ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    override fun findBackPath(kioskId: String, sourceId: String): List<ActionPathDto> {
        val nodesList = graphRepository.findBackPathByTitle(kioskId, sourceId)
            .ifEmpty { throw PathNotFoundException() }
            .drop(1)

        return ActionPathDto.toPathDtoList(nodesList)
    }

    override fun findCategoryNodeId(kioskId: String, id: String): String {
        val entity = graphRepository.findIncomingHasTo(kioskId, id)
            ?: throw NodeNotFoundException()

        return entity.id
    }

    override fun findPlace(kioskId: String, id: String, place: String): ActionPathDto? {
        val entity = graphRepository.findPlaceByTitle(kioskId, id, place) ?: return null

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    override fun findRoot(kioskId: String): ActionPathDto {
        val entity = graphRepository.findRootNode(kioskId) ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    override fun findStation(kioskId: String): ActionPathDto {
        val entity = graphRepository.findStationNode(kioskId) ?: throw NodeNotFoundException()

        return ActionPathDto(
            id = entity.id, title = entity.title,
            x = entity.x, y = entity.y
        )
    }

    override fun isBackRel(kioskId: String, sourceId: String): Boolean {
        val result = graphRepository.isBackRel(kioskId, sourceId) ?: throw PathNotFoundException()

        return result
    }

    override fun changeTitle(nodeId: String, kioskId: String, title: String) {
        graphRepository.changeTitleById(nodeId, kioskId, title) ?: throw NodeNotFoundException()
    }

    override fun deleteMenusByCategory(kioskId: String, id: String) {
        graphRepository.deleteMenuNode(id, kioskId)
    }
}