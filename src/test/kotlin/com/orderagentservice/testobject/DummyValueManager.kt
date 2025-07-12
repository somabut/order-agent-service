package com.orderagentservice.testobject

import com.orderagentservice.global.model.dto.RatioCoordinate
import com.orderagentservice.order.model.dto.BoundingBoxDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.dto.OmniUiComponentDto
import org.springframework.stereotype.Component

class DummyValueManager {
    companion object {
        fun getMenuInfoList(testNum: Int): List<MenuInfoDto> {
            val result = mutableListOf<MenuInfoDto>()
            when(testNum) {
                1 -> {
                    result.add(MenuInfoDto(title = "크리스피 클래식", options = listOf(), category = "햄버거"))
                    result.add(MenuInfoDto(title = "크리스퍼 팩", options = listOf(), category = "햄버거"))
                    result.add(MenuInfoDto(title = "빅맥", options = listOf(), category = "햄버거"))
                    result.add(MenuInfoDto(title = "핫 토마토 모짜볼", options = listOf(), category = "사이드"))
                }
                2 -> {
                    result.add(MenuInfoDto(title = "핫 토마토 모짜볼", options = listOf(), category = "사이드"))
                    result.add(MenuInfoDto(title = "바삭킹", options = listOf(), category = "사이드"))
                    result.add(MenuInfoDto(title = "코코넛슈림프", options = listOf(), category = "사이드"))
                    result.add(MenuInfoDto(title = "칠리소스", options = listOf(), category = "소스"))
                }
                3 -> {
                    result.add(MenuInfoDto(title = "칠리소스", options = listOf(), category = "소스"))
                    result.add(MenuInfoDto(title = "시즈닝", options = listOf(), category = "소스"))
                    result.add(MenuInfoDto(title = "핫 토마토 모짜볼", options = listOf(), category = "사이드"))
                }
            }
            return result
        }

        fun getOmniUiComponentList(testNum: Int): List<OmniUiComponentDto> {
            val result = mutableListOf<OmniUiComponentDto>()
            result.add(OmniUiComponentDto(contents = listOf("사이드"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.289, 0.234, 0.310, 0.310), width = 100, height = 50)),)
            result.add(OmniUiComponentDto(contents = listOf("소스"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.289, 0.234, 0.310, 0.310), width = 100, height = 50)),)
            result.add(OmniUiComponentDto(contents = listOf("음료"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.115, 0.234, 0.310, 0.310), width = 100, height = 50)))

            when(testNum) {
                1 -> {
                    result.add(OmniUiComponentDto(contents = listOf("크리스피 클래식 5600원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.145, 0.310, 0.310, 0.310), width = 100, height = 50)))
                    result.add(OmniUiComponentDto(contents = listOf("크리스퍼 팩 7900원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.212, 0.485, 0.310, 0.310), width = 100, height = 50)),)
                    result.add(OmniUiComponentDto(contents = listOf("빅맥 7900원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.212, 0.485, 0.310, 0.310), width = 100, height = 50)))
                }
                2 -> {
                    result.add(OmniUiComponentDto(contents = listOf("핫 토마토 모짜볼 4800원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.298, 0.572, 0.310, 0.310), width = 100, height = 50)),)
                    result.add(OmniUiComponentDto(contents = listOf("바삭킹 4000원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.269, 0.231, 0.310, 0.310), width = 100, height = 50)),)
                    result.add(OmniUiComponentDto(contents = listOf("군만두 4000원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.269, 0.231, 0.310, 0.310), width = 100, height = 50)),)
                    result.add(OmniUiComponentDto(contents = listOf("코코넛슈림프 3600원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.357, 0.698, 0.310, 0.310), width = 100, height = 50)),)
                }
                3 -> {
                    result.add(OmniUiComponentDto(contents = listOf("칠리소스 700원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.401, 0.633, 0.310, 0.310), width = 100, height = 50)),)
                    result.add(OmniUiComponentDto(contents = listOf("파마산 치즈 600원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.289, 0.914, 0.310, 0.310), width = 100, height = 50)),)
                    result.add(OmniUiComponentDto(contents = listOf("시즈닝 600원"), bbox = BoundingBoxDto(coordinate = RatioCoordinate(0.289, 0.914, 0.310, 0.310), width = 100, height = 50)),)
                }
            }
            return result
        }
    }
}