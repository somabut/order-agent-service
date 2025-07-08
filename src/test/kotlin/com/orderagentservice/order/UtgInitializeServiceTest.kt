package com.orderagentservice.order

import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.UtgInitializeService
import com.orderagentservice.order.util.UiExtractorManager
import com.orderagentservice.testobject.DummyValueManager
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.io.File

@SpringBootTest
class UtgInitializeServiceTest {
    @MockitoBean
    lateinit var uiExtractorManager: UiExtractorManager

    @MockitoBean
    lateinit var notificationService: NotificationService

    @Autowired
    lateinit var utgInitializeService: UtgInitializeService

    @Test
    fun `주어진 메뉴를 기반으로 UTG를 생성한다`() {
        val url = "testUrl"
        val kioskId = "test2"

        BDDMockito.given(notificationService.sendCaptureCommand(any())).willReturn(File(""))
        BDDMockito.given(notificationService.sendActionCommand(any(), any())).willReturn(Pair(-1, -1))

//        val menuList1 = DummyValueManager.getMenuInfoList(1)
//        val omniList1 = DummyValueManager.getOmniUiComponentList(1)
//        BDDMockito.given(uiExtractorManager.queryUiExtractor(any<File>())).willReturn(omniList1)
//        val actionList1 = utgInitializeService.initializeGraph(kioskId, url, menuList1, listOf())

//        val menuList2 = DummyValueManager.getMenuInfoList(2)
//        val omniList2 = DummyValueManager.getOmniUiComponentList(2)
//        BDDMockito.given(uiExtractorManager.queryUiExtractor(any<File>())).willReturn(omniList2)
//        val actionList2 = utgInitializeService.initializeGraph(kioskId, url, menuList2, listOf())
//        for (ele in actionList2) println(ele)

//        val menuList3 = DummyValueManager.getMenuInfoList(3)
//        val omniList3 = DummyValueManager.getOmniUiComponentList(3)
//        BDDMockito.given(uiExtractorManager.queryUiExtractor(any<File>())).willReturn(omniList3)
//        val actionList3 = utgInitializeService.initializeGraph(kioskId, url, menuList3, listOf())
//
//        BDDMockito.verify(uiExtractorManager).queryUiExtractor(any<File>())
    }
}