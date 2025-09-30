package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.StrategyType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class UtgActionFactory @Autowired constructor(
    private val categorySelectStrategy: CategorySelectStrategy,
    private val menuSelectStrategy: MenuSelectStrategy,

    @Qualifier(StrategyType.EX_OPTION)
    private val excludeOptionSelectStrategy: OptionSelectStrategy,

    @Qualifier(StrategyType.IN_OPTION)
    private val includeOptionSelectStrategy: OptionSelectStrategy,

    @Qualifier(StrategyType.EX_BACK)
    private val excludeBackSelectStrategy: BackSelectStrategy,

    @Qualifier(StrategyType.IN_BACK)
    private val includeBackSelectStrategy: BackSelectStrategy
) {
    fun createProfile(utgStrategyRequest: UtgStrategyRequest): UtgActionProfile {
        val optionSelectStrategy = if (utgStrategyRequest.optionStrategy == StrategyType.IN_OPTION) {
            this.includeOptionSelectStrategy
        } else {
            this.excludeOptionSelectStrategy
        }

        val backSelectStrategy = if (utgStrategyRequest.backStrategy == StrategyType.IN_BACK) {
            this.includeBackSelectStrategy
        } else {
            this.excludeBackSelectStrategy
        }

        return UtgActionProfile(
            categorySelectStrategy = categorySelectStrategy,
            menuSelectStrategy = menuSelectStrategy,
            optionSelectStrategy = optionSelectStrategy,
            backSelectStrategy = backSelectStrategy
        )
    }
}