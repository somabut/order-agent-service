package com.orderagentservice.global.config

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter

@ReadingConverter
class StringToBooleanConverter : Converter<String, Boolean> {
    override fun convert(source: String): Boolean {
        return source.equals("True", ignoreCase = true)
    }
}