package com.orderagentservice.order.repository

import com.orderagentservice.global.model.response.ApiResponse
import com.orderagentservice.order.exception.KioskAdminSignInException
import com.orderagentservice.order.exception.MenuInfoRequestException
import com.orderagentservice.order.model.request.KioskAdminSignInRequest
import com.orderagentservice.order.model.response.KioskAdminSingInResponse
import com.orderagentservice.order.model.response.MenuInfoResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Repository
class MenuRepository @Autowired constructor(
    private val env: Environment
) {
    private val KIOSK_ADMIN_HOST = env.getProperty("kiosk-admin.host")
    private val KIOSK_ADMIN_PORT = env.getProperty("kiosk-admin.port")

    fun findAllMenus(kioskId: String, signInRequest: KioskAdminSignInRequest): MenuInfoResponse {
        val restTemplate = RestTemplate()
        val url = "$KIOSK_ADMIN_HOST:$KIOSK_ADMIN_PORT/v1/admin/kiosks/$kioskId/menu-data"
        val accessToken = signIn(signInRequest)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(accessToken)
        val entity = HttpEntity<String>(headers)

        val response = restTemplate.exchange(
            url, HttpMethod.GET,
            entity, object : ParameterizedTypeReference<ApiResponse<MenuInfoResponse>>() {}
        ).body!!.data ?: throw MenuInfoRequestException()

        return response
    }

    private fun signIn(signInRequest: KioskAdminSignInRequest): String {
        val restTemplate = RestTemplate()
        val url = "$KIOSK_ADMIN_HOST:$KIOSK_ADMIN_PORT/v1/auth/login"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request: HttpEntity<KioskAdminSignInRequest> = HttpEntity<KioskAdminSignInRequest>(signInRequest, headers)

        val response = restTemplate.exchange(
            url, HttpMethod.POST,
            request,
            object : ParameterizedTypeReference<ApiResponse<KioskAdminSingInResponse>>() {}
        ).body!!.data ?: throw KioskAdminSignInException()

        return response.accessToken
    }
}