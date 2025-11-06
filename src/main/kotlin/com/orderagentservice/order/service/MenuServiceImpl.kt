package com.orderagentservice.order.service

import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.log.MenuGetLog
import com.orderagentservice.order.repository.MenuRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.math.log

@Service
class MenuServiceImpl @Autowired constructor(
    private val menuRepository: MenuRepository,
) : MenuService {
    private val log = logger()

    override fun getMenus(kioskId: String, accessToken: String): List<MenuInfoDto> {
        log.info("메뉴 정보를 얻어옵니다. 키오스크 ID: ${kioskId}")
        val response = menuRepository.findAllMenus(kioskId, accessToken)
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