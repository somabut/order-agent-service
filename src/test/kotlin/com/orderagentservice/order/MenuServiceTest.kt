package com.orderagentservice.order

import com.orderagentservice.order.model.request.KioskAdminSignInRequest
import com.orderagentservice.order.repository.MenuRepository
import com.orderagentservice.order.service.MenuService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MenuServiceTest @Autowired constructor(
    private val menuService: MenuService
) {
    @Test
    fun `메뉴정보를 받아온다`() {
        val kioskId = "kiosk-8f22eeb6-a920-44ed-af26-d800756fb283"
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2UxMjMiLCJpYXQiOjE3NTMzNjM0NzgsImV4cCI6MTc1MzQ0OTg3OH0.BTBUWWfazVxAdhHW1IrX_Dh3z4SWm04MpMLI0W37E3Db2FRIxB2_5WvDwZjjrBbLPSX_o_QWMod5QvvynvqAeA"
        val result = menuService.getMenus(kioskId, accessToken)
        for (ele in result)
            println(ele)
    }

    @Test
    fun `특정 카테고리의 메뉴 정보를 받아온다`() {
        val kioskId = "kiosk-d89e07fa-4361-4b6a-a550-ac580a1ba195"
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2UxMjMiLCJpYXQiOjE3NTQyODMyMjYsImV4cCI6MTc1NDM2OTYyNn0.KMf9GW183z8sP6IPO7Pc93DrwW-KVDDrax0uH51zTNTefUwuCAs5t5klXljAYhtOcq-G6SpWpiEcKMnl_kVdIQ"
        val result = menuService.getMenusByCategory(kioskId, "빽스치노", accessToken)
        for (ele in result)
            println(ele)
    }
}