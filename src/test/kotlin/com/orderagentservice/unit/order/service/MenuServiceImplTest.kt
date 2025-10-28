package com.orderagentservice.unit.order.service

import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.response.Category
import com.orderagentservice.order.model.response.Menu
import com.orderagentservice.order.model.response.MenuInfoResponse
import com.orderagentservice.order.model.response.Option
import com.orderagentservice.order.repository.MenuRepository
import com.orderagentservice.order.service.MenuService
import com.orderagentservice.order.service.MenuServiceImpl
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.verify

@ExtendWith(MockKExtension::class)
class MenuServiceImplTest {
    @MockK
    private lateinit var menuRepository: MenuRepository

    private lateinit var menuService: MenuService

    private val MOCK_KIOSK_ID = "kiosk-123"
    private val MOCK_TOKEN = "test-access-token"

    private val MOCK_REPO_RESPONSE = MenuInfoResponse(
        kioskId = MOCK_KIOSK_ID,
        categoryCount = 2,
        menuCount = 3,
        optionCount = 6,
        categories = listOf(
            Category(
                categoryId = "cat-1",
                name = "커피", // 이 'name' 필드가 테스트에서 사용됨
                description = "커피 음료",
                createdAt = "2025-10-26T00:00:00Z",
                updatedAt = "2025-10-26T00:00:00Z",
                menus = listOf(
                    Menu(
                        menuId = "menu-101",
                        name = "아메리카노", // 이 'name' 필드가 테스트에서 사용됨
                        description = "기본 커피",
                        price = 4000,
                        saleActiveState = true,
                        saleFailReason = null,
                        createdAt = "2025-10-26T00:00:00Z",
                        updatedAt = "2025-10-26T00:00:00Z",
                        options = listOf(
                            Option(
                                optionId = "opt-1",
                                name = "Hot", // 이 'name' 필드가 테스트에서 사용됨
                                description = "따뜻하게",
                                price = 0,
                                saleActiveState = true,
                                saleFailReason = null,
                                createdAt = "2025-10-26T00:00:00Z",
                                updatedAt = "2025-10-26T00:00:00Z"
                            ),
                            Option(
                                optionId = "opt-2",
                                name = "Ice", // 이 'name' 필드가 테스트에서 사용됨
                                description = "차갑게",
                                price = 500,
                                saleActiveState = true,
                                saleFailReason = null,
                                createdAt = "2025-10-26T00:00:00Z",
                                updatedAt = "2025-10-26T00:00:00Z"
                            )
                        )
                    ),
                    Menu(
                        menuId = "menu-102",
                        name = "카페라떼", // 이 'name' 필드가 테스트에서 사용됨
                        description = "우유 커피",
                        price = 4500,
                        saleActiveState = true,
                        saleFailReason = null,
                        createdAt = "2025-10-26T00:00:00Z",
                        updatedAt = "2025-10-26T00:00:00Z",
                        options = listOf(
                            Option(
                                optionId = "opt-3",
                                name = "Hot",
                                description = "따뜻하게",
                                price = 0,
                                saleActiveState = true,
                                saleFailReason = null,
                                createdAt = "2025-10-26T00:00:00Z",
                                updatedAt = "2025-10-26T00:00:00Z"
                            ),
                            Option(
                                optionId = "opt-4",
                                name = "Ice",
                                description = "차갑게",
                                price = 500,
                                saleActiveState = true,
                                saleFailReason = null,
                                createdAt = "2025-10-26T00:00:00Z",
                                updatedAt = "2025-10-26T00:00:00Z"
                            ),
                            Option(
                                optionId = "opt-5",
                                name = "두유 변경",
                                description = "고소하게",
                                price = 300,
                                saleActiveState = true,
                                saleFailReason = null,
                                createdAt = "2025-10-26T00:00:00Z",
                                updatedAt = "2025-10-26T00:00:00Z"
                            )
                        )
                    )
                )
            ),
            Category(
                categoryId = "cat-2",
                name = "디저트", // 이 'name' 필드가 테스트에서 사용됨
                description = "케이크류",
                createdAt = "2025-10-26T00:00:00Z",
                updatedAt = "2025-10-26T00:00:00Z",
                menus = listOf(
                    Menu(
                        menuId = "menu-201",
                        name = "치즈케이크", // 이 'name' 필드가 테스트에서 사용됨
                        description = "맛있는 케이크",
                        price = 6000,
                        saleActiveState = true,
                        saleFailReason = null,
                        createdAt = "2025-10-26T00:00:00Z",
                        updatedAt = "2025-10-26T00:00:00Z",
                        options = listOf(
                            Option(optionId = "opt-6", name = "포크", description = "1개", price = 0, saleActiveState = true, saleFailReason = null, createdAt = "2025-10-26T00:00:00Z", updatedAt = "2025-10-26T00:00:00Z")
                        )
                    )
                )
            )
        )
    )

    private val EXPECTED_MENU_INFO_LIST = listOf(
        MenuInfoDto(
            title = "아메리카노",
            options = listOf("Hot", "Ice"),
            category = "커피"
        ),
        MenuInfoDto(
            title = "카페라떼",
            options = listOf("Hot", "Ice", "두유 변경"),
            category = "커피"
        ),
        MenuInfoDto(
            title = "치즈케이크",
            options = listOf("포크"),
            category = "디저트"
        )
    )

    @BeforeEach
    fun setUp() {
        menuService = MenuServiceImpl(menuRepository)
    }

    @Test
    @DisplayName("getMenus: 레포지토리에서 받은 중첩 메뉴 목록을 평탄화된 MenuInfoDto 리스트로 변환한다")
    fun `getMenus should transform nested repository response to flat MenuInfoDto list`() {
        // given
        every {
            menuRepository.findAllMenus(MOCK_KIOSK_ID, MOCK_TOKEN)
        } returns MOCK_REPO_RESPONSE

        // when
        val actualResult = menuService.getMenus(MOCK_KIOSK_ID, MOCK_TOKEN)

        // then
        verify(exactly = 1) { menuRepository.findAllMenus(MOCK_KIOSK_ID, MOCK_TOKEN) }
        assertThat(actualResult)
            .isNotNull
            .hasSize(3)
            .isEqualTo(EXPECTED_MENU_INFO_LIST)
    }
}