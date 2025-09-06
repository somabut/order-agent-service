package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.service.MenuService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PageChecker @Autowired constructor(
    private val wordSimilarityService: WordSimilarityService,
    private val menuService: MenuService
) {
    fun checkMenuPage(menuDto: MenuInfoDto, menuList: List<MenuInfoDto>, uiList: List<UiComponentDto>): Boolean {
        val sourceList = getMenusByCategory(menuDto.category, menuList)

        val result = wordSimilarityService.determinePage(sourceList, uiList)
        return result
    }

    fun checkOptionPage(optionList: List<String>, uiList: List<UiComponentDto>): Boolean {
        val result = wordSimilarityService.determinePage(optionList, uiList)
        return result
    }

    private fun getMenusByCategory(category: String, menuList: List<MenuInfoDto>): List<String> {
        val filteredList = menuList
            .filter { it.category == category }
            .map { it.title }

        return filteredList
    }
}