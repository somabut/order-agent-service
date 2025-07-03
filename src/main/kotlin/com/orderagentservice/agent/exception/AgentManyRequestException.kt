package com.orderagentservice.agent.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException
import kotlin.math.E

class AgentManyRequestException(
    override val errorCode: ErrorCode = ErrorCode.AGENT_MANY_REQUEST
) : RootException(errorCode)