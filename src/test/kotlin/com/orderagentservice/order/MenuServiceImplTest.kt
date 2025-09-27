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
        val kioskId = "kiosk-259f9d77-c870-4f10-af88-3e67ca2d1c3f"
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2UxMjMiLCJpYXQiOjE3NTg5NTQwNDYsImV4cCI6MTc1ODk1NzY0Nn0.RglECJSiV-WZMLTNbhAjwXPdUnRzKk2dhPKTIAhXDtzmm8Kt-_NUXWYzxASpqhD5KxsamVTW-VWIVUEwbdz9Iw"
        val result = menuService.getMenus(kioskId, accessToken)
        for (ele in result)
            println(ele)
    }

    @Test
    fun `특정 카테고리의 메뉴 정보를 받아온다`() {
        val kioskId = "kiosk-2303452c-8454-4b9f-add2-47314cfd3911"
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2UxMjMiLCJpYXQiOjE3NTY1MzIwMzIsImV4cCI6MTc1NjYxODQzMn0.6jenARBMW5pylsZcayf4uSUASBs3e_Jz2Rp0pG2MMf4Bqdc_sIYIwgmtWgiVsLzINo41B5St1F7qPiFgWwe4IA"
        val result = menuService.getMenusByCategory(kioskId, "음료", accessToken)
        for (ele in result)
            println(ele)
    }

    @Test
    fun `특정 카테고리의 메뉴 정보를 받아온다 - 2`() {
        val kioskId = "kiosk-2303452c-8454-4b9f-add2-47314cfd3911"
        val accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJxd2UxMjMiLCJpYXQiOjE3NTY1MzgyMjUsImV4cCI6MTc1NjYyNDYyNX0.hEStIJkSrnkemiHn1bSOQ7izT9oQWk0Q6-9XsY2qyOg8BixaPTfNWqa_AjkpoqrKg1kG5l1OcCHVBnCYJCt7rw"

        val menuList = menuService.getMenus(kioskId, accessToken)
        val menuDto = menuList.first { it.title == "달콤아이스티" }
        val filteredList = menuList.filter { it.category == menuDto.category }

        for (ele in filteredList) {
            println(ele)
        }
    }
}