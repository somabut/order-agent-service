package com.orderagentservice.global.model

enum class LogType (
    val message: String
){
    ORDER_START("자동 주문 시작"),
    ACTION_RESULT("액션 결과"),
    ORDER_RESULT("주문 결과"),

}