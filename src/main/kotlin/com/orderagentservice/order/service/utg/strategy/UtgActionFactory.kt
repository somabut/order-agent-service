package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.logger
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

    @Qualifier(StrategyType.OP_BACK)
    private val optionalBackSelectStrategy: BackSelectStrategy,

    @Qualifier(StrategyType.IN_BACK)
    private val includeBackSelectStrategy: BackSelectStrategy,

    @Qualifier(StrategyType.IN_PAYMENT_PLACE)
    private val placePaymentSelectStrategy: PlacePaymentSelectStrategy,

    @Qualifier(StrategyType.EX_PAYMENT_PLACE)
    private val paymentSelectStrategy: PaymentSelectStrategy
) {
    private val log = logger()

    fun createProfile(utgStrategyRequest: UtgStrategyRequest): UtgActionProfile {
        val startSelectStrategy = if (utgStrategyRequest.startStrategy == StrategyType.IN_START_PLACE) {
            log.info("IN_START_PLACE 선택")
            this.placeStartSelectStrategy
        } else {
            log.info("EX_START_PLACE 선택")
            this.startSelectStrategy
        }

        val optionSelectStrategy = if (utgStrategyRequest.optionStrategy == StrategyType.IN_OPTION) {
            log.info("IN_OPTION 선택")
            this.includeOptionSelectStrategy
        } else {
            log.info("EX_OPTION 선택")
            this.excludeOptionSelectStrategy
        }

        val backSelectStrategy = if (utgStrategyRequest.backStrategy == StrategyType.IN_BACK) {
            log.info("IN_BACK 선택")
            this.includeBackSelectStrategy
        } else if (utgStrategyRequest.backStrategy == StrategyType.OP_BACK) {
            log.info("OP_BACK 선택")
            this.optionalBackSelectStrategy
        } else {
            log.info("EX_BACK 선택")
            this.excludeBackSelectStrategy
        }

        val paymentSelectStrategy = if (utgStrategyRequest.paymentStrategy == StrategyType.IN_PAYMENT_PLACE) {
            log.info("IN_PAYMENT_PLACE 선택")
            this.placePaymentSelectStrategy
        } else {
            log.info("EX_PAYMENT_PLACE 선택")
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