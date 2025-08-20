package com.orderagentservice.order.service.auto

import com.orderagentservice.logger
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.AutoOrderResultDto
import com.orderagentservice.order.service.graph.GraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AutoOrderService @Autowired constructor(
    private val graphService: GraphService,
    private val globalLogger: GlobalLogger,
    private val autoTaskExecutor: AutoTaskExecutor,
    private val orderLogSender: OrderLogSender
) {
    private val log = logger()

    fun execute(kioskId: String, taskId: String, orderRequest: AutoOrderRequest): AutoOrderResultDto {
        val requestJson = jsonMapper.writeValueAsString(orderRequest)
        orderLogSender.logOrder(kioskId, taskId, "자동 주문을 시작합니다. 주문: ${requestJson}")
        globalLogger.loggingOrderStart(kioskId, taskId)
        val startTime = System.nanoTime()

        //루트 노드 가져오기
        val nowNodeId = graphService.findRoot(kioskId).id

        val context = AutoOrderContext.toBasicContext(
            kioskId = kioskId, taskId = taskId, nodeId = nowNodeId,
            place = orderRequest.place
        )

        //메뉴 담기
        putMenus(
            context = context,
            menuList = orderRequest.autoOrderMenus
        )

        //결제하기
        proceedPayment(context)

        val endTime = System.nanoTime()
        val processingTime = (endTime - startTime) / 1000000
        context.history.processingTime += processingTime

        orderLogSender.logOrder(kioskId, taskId, "자동 주문 완료. 수행시간: ${processingTime}ms")
        globalLogger.loggingOrderResult(
            kioskId = kioskId, menuList = context.history,
            processingTime = processingTime, paymentMethod = orderRequest.payment,
            taskId = taskId
        )

        return context.history
    }

    private fun putMenus(context: AutoOrderContext, menuList: List<AutoOrderMenu>) {
        for (menu in menuList) {
            //포장/매장 클릭
            if (context.isPlaceSelected == false) {
                context.isPlaceSelected = autoTaskExecutor.clickPlace(context)
            }

            //메뉴 클릭
            var lastNodeId = autoTaskExecutor.clickMenu(context, menu).id

            //메뉴에 BACK_TO관계가 없으면 모달, 옵션 없는 것으로 판단
            val isNextPage = graphService.isBackRel(context.kioskId, lastNodeId)

            //옵션 클릭
            lastNodeId = autoTaskExecutor.clickOption(context, menu.autoOrderOptions, lastNodeId)

            //돌아가는 UI 클릭
            if (isNextPage) {
                context.nodeId = autoTaskExecutor.clickBack(context, lastNodeId)
            } else {
                context.nodeId = graphService.findNodeByTitle(context.kioskId, menu.category)
            }

//            context.nodeId = autoTaskExecutor.clickBack(context, lastNodeId)
            log.info("현재 노드 ID: ${context.nodeId}")
        }
    }

    private fun proceedPayment(context: AutoOrderContext) {
        val actionList = graphService.findPath(context.kioskId, context.nodeId, "complete").toMutableList()
        actionList.removeLast()

        for (act in actionList) {
            //포장/매장 클릭
            if (context.isPlaceSelected == false) {
                context.isPlaceSelected = autoTaskExecutor.clickPlace(context)
            }

            //결제 클릭
            autoTaskExecutor.clickPayment(context, act)
        }
    }
}