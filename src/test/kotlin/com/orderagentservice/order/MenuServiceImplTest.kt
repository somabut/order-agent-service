package com.orderagentservice.order

import com.orderagentservice.order.service.MenuService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MenuServiceImplTest @Autowired constructor(
    private val menuService: MenuService
) {
    @Test
    fun `메뉴정보를 받아온다`() {
        val kioskId = "kiosk-494cbae2-0681-46e6-ba59-88726509fd88"
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzb21hMTMyNDg5NyIsImlhdCI6MTc1NTE3NDQzOCwiZXhwIjoxNzU1MjYwODM4fQ.5jL5U-2IE1-LABEyL15OMeSg0c4JvrzjPandIzmQV4eHJdZlz4c2uvDIuSb9xWA025swW03ffZncQldaFUIIMQ"
        val result = menuService.getMenus(kioskId, accessToken)
        for (ele in result)
            println(ele)
    }

    @Test
    fun `특정 카테고리의 메뉴 정보를 받아온다`() {
        val kioskId = "kiosk-d89e07fa-4361-4b6a-a550-ac580a1ba195"
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2UxMjMiLCJpYXQiOjE3NTQzODE1ODAsImV4cCI6MTc1NDQ2Nzk4MH0.CbepFStBYxo-8yS9hjj5gf9P0ejUwpRBLxxuhG_4RsV5fzlhSaXIL6GqWaMVnPbj0lzAPvvYzuTvUcR1u6mr6g"
        val result = menuService.getMenusByCategory(kioskId, "빽스치노", accessToken)
        for (ele in result)
            println(ele)
    }
}