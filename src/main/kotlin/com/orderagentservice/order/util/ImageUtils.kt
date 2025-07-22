package com.orderagentservice.order.util

import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class ImageUtils {
    companion object {
        fun imageToHash(image: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(image.readBytes())
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }
}