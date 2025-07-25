package com.orderagentservice.global.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class HttpStatusFlexibleDeserializer : JsonDeserializer<HttpStatus>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): HttpStatus? {
        val value = p.text?.trim() ?: return null

        return when {
            // 숫자인지 확인 (예: "200", "404", "500" 등)
            value.matches(Regex("\\d+")) -> {
                val code = value.toIntOrNull()
                code?.let { HttpStatus.resolve(it) }
            }
            // 문자열인 경우 (예: "OK", "NOT_FOUND", "INTERNAL_SERVER_ERROR" 등)
            else -> {
                try {
                    HttpStatus.valueOf(value.uppercase())
                } catch (e: IllegalArgumentException) {
                    // 알 수 없는 값일 경우 null 반환 (또는 원하는 기본값)
                    null
                }
            }
        }
    }
}