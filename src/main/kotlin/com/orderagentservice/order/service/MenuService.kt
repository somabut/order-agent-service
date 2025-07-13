package com.orderagentservice.order.service

import com.orderagentservice.logger
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.request.KioskAdminSignInRequest
import com.orderagentservice.order.repository.MenuRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MenuService @Autowired constructor(
    private val menuRepository: MenuRepository
) {
    private val log = logger()

    fun getMenus(kioskId: String, signInRequest: KioskAdminSignInRequest): List<MenuInfoDto> {
        val response = menuRepository.findAllMenus(kioskId, signInRequest)
        val menuInfoList = mutableListOf<MenuInfoDto>()

        for (category in response.categories) {
            for (menu in category.menus) {
                val options = mutableListOf<String>()
                menu.options.forEach { options.add(it.name) }

                val dto = MenuInfoDto(
                    title = menu.name,
                    options = options,
                    category = category.name
                )
                menuInfoList.add(dto)
            }
        }
        return menuInfoList
    }
}