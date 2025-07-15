package com.orderagentservice.order.service

import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderOption
import com.orderagentservice.order.model.request.AutoOrderRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class RandomTaskService @Autowired constructor(
    private val menuService: MenuService
) {
    fun generate(count: Int, kioskId: String, accessToken: String): AutoOrderRequest {
        val menuList = menuService.getMenus(kioskId, accessToken)

        val selectMenuList = mutableListOf<AutoOrderMenu>()
        val menuIndexList = getRandomValue(menuList.size, count)

        for (menuIndex in menuIndexList) {
            val selectOptionList = mutableListOf<AutoOrderOption>()
            val options = menuList[menuIndex].options

            if (options.size > 0) {
                val optionCount = Random.nextInt(0, options.size + 1)
                val optionIndexList = getRandomValue(options.size, optionCount)
                for (optIndex in optionIndexList) {
                    selectOptionList.add(
                        AutoOrderOption(
                            title = options[optIndex],
                            count = 1
                        )
                    )
                }
            }

            val title = menuList[menuIndex].title
            val category = menuList[menuIndex].category
            selectMenuList.add(
                AutoOrderMenu(
                    category = category,
                    title = title,
                    count = Random.nextInt(1, 4),
                    autoOrderOptions = selectOptionList
                )
            )
        }
        return AutoOrderRequest(autoOrderMenus = selectMenuList, place = "포장", payment = "카드")
    }

    private fun getRandomValue(max: Int, count: Int): List<Int> {
        val random = Random(System.nanoTime())
        return (0 until max).shuffled(random).take(count)
    }
}