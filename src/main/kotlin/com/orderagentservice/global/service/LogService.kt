package com.orderagentservice.global.service

import com.orderagentservice.global.exception.InvalidSessionException
import com.orderagentservice.global.model.dto.LogDto
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.model.response.MenuInfoResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class LogService @Autowired constructor(
    private val env: Environment
) {
    private val ORDER_CHAT_HOST = env.getProperty("order-chat.host")

    fun sendLog(logDto: LogDto): ApiResponse<*> {
        val url = "$ORDER_CHAT_HOST/v1/task/"

        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity<LogDto>(logDto, headers)

        try {
            val response = restTemplate.exchange(
                url, HttpMethod.POST,
                entity, ApiResponse::class.java
            ).body!!
            return response
        } catch (e: HttpClientErrorException) {
            throw InvalidSessionException()
        }
    }
}