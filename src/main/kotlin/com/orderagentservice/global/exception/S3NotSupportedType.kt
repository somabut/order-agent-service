package com.orderagentservice.global.exception

import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.RootException

class S3NotSupportedType(
    override val errorCode: ErrorCode = ErrorCode.GLOBAL_S3_TYPE_ERROR
) : RootException(errorCode)