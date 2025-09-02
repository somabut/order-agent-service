package com.orderagentservice.global.util

import com.orderagentservice.global.exception.S3NotSupportedType

class ImageUtils {
    companion object {
        fun getExtension(contentType: String) =
            when(contentType) {
                "image/png" -> "png"
                "image/jpeg", "image/jpg" -> "jpg"
                else -> throw S3NotSupportedType()
            }
    }
}