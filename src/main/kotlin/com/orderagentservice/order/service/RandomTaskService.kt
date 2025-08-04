package com.orderagentservice.order.service

import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderOption
import com.orderagentservice.order.model.request.AutoOrderRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

@Service
class RandomTaskService @Autowired constructor(
    private val autoOrderService: AutoOrderService,
    private val menuService: MenuService
) {
    private val TEST_TASK_ID = "test-session-bag-coffee-1"

    fun proceed(count: Int, kioskId: String, accessToken: String): List<String> {
        repeat(count) {
            val request = generate(count, kioskId, accessToken)
            autoOrderService.order(kioskId, TEST_TASK_ID, request)
        }
        return listOf()
    }

    fun generate(count: Int, kioskId: String, accessToken: String): AutoOrderRequest {
        val menuList = menuService.getMenus(kioskId, accessToken)

        //메뉴 생성
        val selectMenuList = mutableListOf<AutoOrderMenu>()
        repeat(count) {
            val orderMenu = generateMenu(menuList)
            selectMenuList.add(orderMenu)
        }

        //장소 선택
        val placeOptions = listOf("포장", "매장")
        val randomPlace = placeOptions.random()
        return AutoOrderRequest(autoOrderMenus = selectMenuList, place = randomPlace, payment = "카드")
    }

    private fun generateMenu(menuList: List<MenuInfoDto>): AutoOrderMenu {
        val menuIndex = getRandomValue(menuList.size)[0]

        //옵션 생성
        val options = menuList[menuIndex].options
        var selectOptionList = listOf<AutoOrderOption>()
        if (options.isNotEmpty()) {
            selectOptionList = generateOptions(options)
        }

        return AutoOrderMenu(
            category = menuList[menuIndex].category,
            title = menuList[menuIndex].title,
            count = 1,
            autoOrderOptions = selectOptionList
        )
    }

    private fun generateOptions(options: List<String>): List<AutoOrderOption> {
        val selectOptionList = mutableListOf<AutoOrderOption>()

        val optionSize = Random.nextInt(0, options.size + 1)
        val optionIndexList = getRandomValue(options.size, optionSize)
        for (optIndex in optionIndexList) {
            selectOptionList.add(
                AutoOrderOption(
                    title = options[optIndex],
                    count = 1
                )
            )
        }

        return selectOptionList
    }

    private fun getRandomValue(max: Int, count: Int = 1): List<Int> {
        val random = Random(System.nanoTime())
        return (0 until max).shuffled(random).take(count)
    }
}