package com.orderagentservice.order.service

import com.orderagentservice.order.model.dto.MenuInfoDto

interface MenuService {
    fun getMenus(kioskId: String, accessToken: String): List<MenuInfoDto>

//    fun getMenusByCategory(kioskId: String, category: String, accessToken: String): List<MenuInfoDto>
}