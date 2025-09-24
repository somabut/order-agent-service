package com.orderagentservice.order.service.utg

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.ErrorCode
import com.orderagentservice.global.model.OrderAgentException
import com.orderagentservice.global.model.dto.WordMatchDto
import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.global.util.ImageUtils
import com.orderagentservice.logger
import com.orderagentservice.order.model.request.WordCompareRequest
import com.orderagentservice.order.model.response.DetectorResponse
import com.orderagentservice.order.model.response.ImageCompareResponse
import com.orderagentservice.order.model.response.WordCompareResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class ComparatorManager @Autowired constructor(
    private val env: Environment
) {
    private val COMPARATOR_HOST = env.getProperty("comparator.host")

    fun imageCompare(source: ByteArray, sourceType: String, target: ByteArray, targetType: String): Boolean {
        val restTemplate = RestTemplate()
        val url = "$COMPARATOR_HOST/v1/compare/image"

        val sourceContent = object : ByteArrayResource(source) {
            override fun getFilename(): String {
                return "source.${ImageUtils.getExtension(sourceType)}"
            }
        }
        val targetContent = object : ByteArrayResource(target) {
            override fun getFilename(): String {
                return "target.${ImageUtils.getExtension(targetType)}"
            }
        }

        val body = LinkedMultiValueMap<String, Any>()
        body["source"] = sourceContent
        body["target"] = targetContent

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        try {
            val requestEntity = HttpEntity(body, headers)
            val responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                object : ParameterizedTypeReference<ApiResponse<ImageCompareResponse>>() {}
            )
            val response: ImageCompareResponse = responseEntity.body!!.data!!

            return response.result
        } catch (e: Exception) {
            throw OrderAgentException(errorCode = ErrorCode.ORDER_IMAGE_COMPARE_FAIL)
        }
    }

    fun wordCompare(targetWord: String, candidates: List<UiComponentDto>): WordMatchDto {
        val restTemplate = RestTemplate()
        val url = "$COMPARATOR_HOST/v1/compare/word"

        val requestBody = WordCompareRequest(
            target = targetWord,
            candidates = candidates,
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        try {
            val requestEntity = HttpEntity(requestBody, headers)
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                object : ParameterizedTypeReference<ApiResponse<WordCompareResponse>>() {}
            )

            return response.body!!.data!!.result
        } catch (e: Exception) {
            throw OrderAgentException(errorCode = ErrorCode.ORDER_WORD_COMPARE_FAIL)
        }
    }
}