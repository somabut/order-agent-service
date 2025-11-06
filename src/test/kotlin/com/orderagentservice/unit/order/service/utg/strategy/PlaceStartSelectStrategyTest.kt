package com.orderagentservice.unit.order.service.utg.strategy

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.AllUiComponentDto
import com.orderagentservice.order.model.dto.InfoDto
import com.orderagentservice.order.model.dto.UiDto
import com.orderagentservice.order.model.entity.InfoEntity
import com.orderagentservice.order.model.entity.UiEntity
import com.orderagentservice.order.model.log.NodeSaveLog
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.NodeRelationType
import com.orderagentservice.order.service.graph.info.InfoGraphService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import com.orderagentservice.order.service.utg.UiDetectorManager
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import com.orderagentservice.order.service.utg.strategy.PlaceStartSelectStrategy
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PlaceStartSelectStrategyTest {
    @MockK
    private lateinit var logService: LogService
    @MockK
    private lateinit var uiGraphService: UiGraphService
    @MockK
    private lateinit var infoGraphService: InfoGraphService
    @MockK
    private lateinit var placeUtgService: PlaceUtgService
    @MockK
    private lateinit var uiDetectorManager: UiDetectorManager

    private lateinit var strategy: PlaceStartSelectStrategy

    @BeforeEach
    fun setUp() {
        strategy = PlaceStartSelectStrategy( // ⭐️ 생성자 변경
            logService,
            uiGraphService,
            infoGraphService,
            placeUtgService,
            uiDetectorManager
        )
    }

    @Test
    @DisplayName("execute: setupNode 호출 후, 포장/매장 그래프 초기화 실행")
    fun `execute should setup node and then initialize place graph`() {
        // given
        val kioskId = "kiosk-001"
        val imageName = "img.png"
        val rootNodeId = "root-node-id"
        val stationNodeId = "station-node-id"
        val infoNodeId = "info-node-id"

        val context = mockk<UtgContext>()
        val utgStrategyRequest = mockk<UtgStrategyRequest>(relaxed = true)

        every { context.kioskId } returns kioskId
        every { context.imageName } returns imageName
        every { context.lastNodeId } returns rootNodeId
        every { context.stationNodeId } returns stationNodeId
        every { context.lastNodeId = any() } just runs
        every { context.stationNodeId = any() } just runs

        val mockRootEntity = mockk<UiEntity>()
        val mockStationEntity = mockk<UiEntity>()
        val mockInfoEntity = mockk<InfoEntity>()

        every { mockRootEntity.id } returns rootNodeId
        every { mockStationEntity.id } returns stationNodeId
        every { mockInfoEntity.id } returns infoNodeId

        every { logService.printLog(any<NodeSaveLog>()) } just runs
        every { uiGraphService.saveNode(any<UiDto>()) } returns mockRootEntity andThen mockStationEntity
        every { infoGraphService.saveNode(any<InfoDto>()) } returns mockInfoEntity
        every { infoGraphService.saveRel(kioskId, infoNodeId) } just runs
        every { uiGraphService.saveRel(rootNodeId, stationNodeId, NodeRelationType.PATH_TO) } just runs

        val mockOcrList = listOf(mockk<UiComponentDto>())
        val mockUiDetectDto = mockk<AllUiComponentDto>()

        every { uiDetectorManager.getUiComponents(context, true) } returns mockUiDetectDto
        every { mockUiDetectDto.ocrElements } returns mockOcrList
        every { placeUtgService.initializeGraph(context, mockOcrList) } just runs

        // when
        strategy.execute(context, utgStrategyRequest)

        // then
        verify(exactly = 2) { logService.printLog(any<NodeSaveLog>()) }
        verify(exactly = 2) { uiGraphService.saveNode(any()) }
        verify(exactly = 1) { infoGraphService.saveNode(any<InfoDto>()) }
        verify(exactly = 1) { infoGraphService.saveRel(kioskId, infoNodeId) }
        verify(exactly = 1) { uiGraphService.saveRel(rootNodeId, stationNodeId, NodeRelationType.PATH_TO) }

        verify(exactly = 1) { uiDetectorManager.getUiComponents(context, true) }
        verify(exactly = 1) { placeUtgService.initializeGraph(context, mockOcrList) }

        verify(exactly = 1) { context.lastNodeId = rootNodeId }
        verify(exactly = 1) { context.stationNodeId = stationNodeId }
    }
}