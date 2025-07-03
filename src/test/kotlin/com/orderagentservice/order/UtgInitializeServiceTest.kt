package com.orderagentservice.order

import com.orderagentservice.order.service.UtgInitializeService
import com.orderagentservice.order.util.UiExtractorManager
import com.orderagentservice.testobject.DummyValueManager
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.io.File

@SpringBootTest
class UtgInitializeServiceTest {
    @MockitoBean
    lateinit var uiExtractorManager: UiExtractorManager

    @Autowired
    lateinit var utgInitializeService: UtgInitializeService

    @Test
    fun `주어진 메뉴를 기반으로 UTG를 생성한다`() {
        val url = "testUrl"
        val kioskId = "abc"
        val menuList = DummyValueManager.getMenuInfoList()
        val omniList1 = DummyValueManager.getOmniUiComponentList(1)
        BDDMockito.given(uiExtractorManager.queryUiExtractor(any<File>())).willReturn(omniList1)
        val actionList1 = utgInitializeService.initializeGraph(kioskId, url, menuList, listOf())

        val omniList2 = DummyValueManager.getOmniUiComponentList(2)
        BDDMockito.given(uiExtractorManager.queryUiExtractor(any<File>())).willReturn(omniList2)
        val actionList2 = utgInitializeService.initializeGraph(kioskId, url, menuList, listOf())

        val omniList3 = DummyValueManager.getOmniUiComponentList(3)
        BDDMockito.given(uiExtractorManager.queryUiExtractor(any<File>())).willReturn(omniList3)
        val actionList3 = utgInitializeService.initializeGraph(kioskId, url, menuList, listOf())

        BDDMockito.verify(uiExtractorManager).queryUiExtractor(any<File>())
    }
}