package com.orderagentservice.agent.model

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class UsageTracker {
    var totalUsage: Int = 0
}