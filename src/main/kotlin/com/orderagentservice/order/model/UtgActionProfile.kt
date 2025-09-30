package com.orderagentservice.order.model

import com.orderagentservice.order.service.utg.strategy.BackSelectStrategy
import com.orderagentservice.order.service.utg.strategy.CategorySelectStrategy
import com.orderagentservice.order.service.utg.strategy.MenuSelectStrategy
import com.orderagentservice.order.service.utg.strategy.OptionSelectStrategy

data class UtgActionProfile (
    val categorySelectStrategy: CategorySelectStrategy,
    val menuSelectStrategy: MenuSelectStrategy,
    val optionSelectStrategy: OptionSelectStrategy,
    val backSelectStrategy: BackSelectStrategy
)