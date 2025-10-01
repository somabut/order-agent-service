package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.request.UtgStrategyRequest
import com.orderagentservice.order.model.type.StrategyType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class UtgActionFactory @Autowired constructor(
    @Qualifier(StrategyType.EX_START_PLACE)
    private val startSelectStrategy: StartSelectStrategy,

    @Qualifier(StrategyType.IN_START_PLACE)
    private val placeStartSelectStrategy: StartSelectStrategy,

    private val categorySelectStrategy: CategorySelectStrategy,
    private val menuSelectStrategy: MenuSelectStrategy,

    @Qualifier(StrategyType.EX_OPTION)
    private val excludeOptionSelectStrategy: OptionSelectStrategy,

    @Qualifier(StrategyType.IN_OPTION)
    private val includeOptionSelectStrategy: OptionSelectStrategy,

    @Qualifier(StrategyType.EX_BACK)
    private val excludeBackSelectStrategy: BackSelectStrategy,

    @Qualifier(StrategyType.IN_BACK)
    private val includeBackSelectStrategy: BackSelectStrategy,

    @Qualifier(StrategyType.IN_PAYMENT_PLACE)
    private val placePaymentSelectStrategy: PlacePaymentSelectStrategy,

    @Qualifier(StrategyType.EX_PAYMENT_PLACE)
    private val paymentSelectStrategy: PaymentSelectStrategy
) {
    fun createProfile(utgStrategyRequest: UtgStrategyRequest): UtgActionProfile {
        val startSelectStrategy = if (utgStrategyRequest.startStrategy == StrategyType.IN_START_PLACE) {
            this.placeStartSelectStrategy
        } else {
            this.startSelectStrategy
        }

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

        val paymentSelectStrategy = if (utgStrategyRequest.paymentStrategy == StrategyType.IN_PAYMENT_PLACE) {
            this.placePaymentSelectStrategy
        } else {
            this.paymentSelectStrategy
        }

        return UtgActionProfile(
            startSelectStrategy = startSelectStrategy,
            categorySelectStrategy = categorySelectStrategy,
            menuSelectStrategy = menuSelectStrategy,
            optionSelectStrategy = optionSelectStrategy,
            backSelectStrategy = backSelectStrategy,
            paymentSelectStrategy = paymentSelectStrategy,
        )
    }
}