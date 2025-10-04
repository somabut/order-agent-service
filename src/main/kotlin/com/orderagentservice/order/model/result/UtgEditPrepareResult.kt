package com.orderagentservice.order.model.result

import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.UtgActionProfile
import com.orderagentservice.order.model.UtgContext

data class UtgEditPrepareResult (
    val autoContext: AutoOrderContext,
    val actionProfile: UtgActionProfile
)