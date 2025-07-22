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
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2UxMjMiLCJpYXQiOjE3NTMxNjE1NjQsImV4cCI6MTc1MzI0Nzk2NH0.Bt7OG2VjKsx2ilViKxpytz0rZBmdwCADmdd07i2ziZ3A0_oWDmXOYOia8T1LNdINa7meeEnlieENjKBIax4Bjw"
        val result = menuService.getMenus(kioskId, accessToken)
        for (ele in result)
            println(ele)
    }
}