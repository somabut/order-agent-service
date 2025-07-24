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

    fun findAllMenus(kioskId: String, accessToken: String): MenuInfoResponse {
        val restTemplate = RestTemplate()

        //TODO(임시 url)
//        val url = "$KIOSK_ADMIN_HOST/v1/admin/kiosks/$kioskId/menu-data"
        val url = "$KIOSK_ADMIN_HOST/v1/admin/kiosks/kiosk-d89e07fa-4361-4b6a-a550-ac580a1ba195/menu-data"

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
}