package com.orderagentservice.agent.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException

class AgentManyRequestException(
    override val errorCode: ErrorCode = ErrorCode.AGENT_MANY_REQUEST
) : OrderAgentException(errorCode)