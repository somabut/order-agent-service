package com.orderagentservice.order.service.auto

import com.orderagentservice.order.model.AutoOrderResultDto
import com.orderagentservice.order.model.dto.AutoOrderBenchMarkDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderOption
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.order.service.MenuService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class RandomTaskService @Autowired constructor(
    private val autoOrderService: AutoOrderService,
    private val menuService: MenuService
) {
    private val TEST_TASK_ID = "test-session-bag-coffee-1"

    fun proceed(count: Int, kioskId: String, accessToken: String): List<AutoOrderBenchMarkDto> {
        val benchMarkList = mutableListOf<AutoOrderBenchMarkDto>()

        repeat(count) {
            val request = generate(count, kioskId, accessToken)
            val history = autoOrderService.order(kioskId, TEST_TASK_ID, request)

            //클릭결과와 원래 요청 비교
            val compareResult = compare(request, history)
            benchMarkList.add(
                AutoOrderBenchMarkDto(
                    correct = compareResult.first,
                    wrong = compareResult.second,
                    processingTime = history.processingTime
                )
            )
        }
        return benchMarkList
    }

    fun compare(request: AutoOrderRequest, result: AutoOrderResultDto): Pair<Int, Int> {
        var correct = 0
        var wrong = 0

        if (request.payment == result.payment) {
            correct += 1
        } else {
            wrong += 1
        }

        if (request.place == result.place) {
            correct += 1
        } else {
            wrong += 1
        }

        //메뉴 비교
        val pair = compareMenus(request, result)
        correct += pair.first
        wrong += pair.second

        return Pair(correct, wrong)
    }

    private fun compareMenus(request: AutoOrderRequest, result: AutoOrderResultDto): Pair<Int, Int> {
        var count = 0
        var wrong = 0
        val menuVisit = hashMapOf<MenuInfoDto, Boolean>()

        for (requestMenu in request.autoOrderMenus) {
            var flag = false
            for (resultMenu in result.menus) {
                if (menuVisit[resultMenu] != true) {
                    if (requestMenu.title == resultMenu.title && compareOptions(requestMenu.autoOrderOptions, resultMenu.options)) {
                        count += 1
                        menuVisit[resultMenu] = true
                        flag = true
                        break
                    }
                }
            }

            if (flag == false) {
                wrong += 1
            }
        }

        return Pair(count, wrong)
    }

    private fun compareOptions(requestOptions: List<AutoOrderOption>, resultOptions: List<String>)
        = requestOptions.map { it.title }.toSet() == resultOptions.toSet()

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