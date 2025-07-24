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
}