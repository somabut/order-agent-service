package com.orderagentservice.order.service

import com.orderagentservice.global.model.dto.LogDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.OrderResultDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.request.AutoOrderOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.swing.Action

@Service
class AutoOrderService @Autowired constructor(
    private val notificationService: NotificationService,
    private val logService: LogService,
    private val utgService: UtgService,
    private val globalLogger: GlobalLogger
) {
    private val log = logger()

    fun order(kioskId: String, taskId: String, orderRequest: AutoOrderRequest) {
        val requestJson = jsonMapper.writeValueAsString(orderRequest)
        logOrder(kioskId, taskId, "자동 주문을 시작합니다. 주문: ${requestJson}")
        globalLogger.loggingOrderStart(kioskId, taskId)
        val startTime = System.nanoTime()

        //루트 노드 가져오기
        val nowNodeId = utgService.findRootNode(kioskId).id

        val context = AutoOrderContext(
            kioskId = kioskId,
            taskId = taskId,
            nodeId = nowNodeId,
            place = orderRequest.place,
            isPlace = (orderRequest.place == null)
        )

        //메뉴 담기
        val history = putMenus(
            context = context,
            menuList = orderRequest.autoOrderMenus
        )

        //결제하기
        proceedPayment(context)

        val endTime = System.nanoTime()
        val processingTime = (endTime - startTime) / 1000000
        logOrder(kioskId, taskId, "자동 주문 완료. 수행시간: ${processingTime}ms")
        logOrder(kioskId, taskId, "최종 주문 정보: ${history}")
        globalLogger.loggingOrderResult(
            kioskId = kioskId, menuList = history,
            processingTime = processingTime, paymentMethod = orderRequest.payment,
            taskId = taskId
        )
    }

    private fun putMenus(context: AutoOrderContext, menuList: List<AutoOrderMenu>): List<OrderResultDto> {
        val kioskId = context.kioskId
        val history = mutableListOf<OrderResultDto>()
        for (menu in menuList) {
            //포장/매장 클릭
            if (context.isPlace == false) {
                context.isPlace = clickPlace(context)
            }

            //메뉴 클릭
            val last = clickMenu(menu, context)

            //옵션 클릭
            clickOption(menu.autoOrderOptions, last, context)

            //돌아가는 UI 클릭
            clickBack(last, context)

            //현재 노드 갱신
            context.nodeId = utgService.findCategoryNodeId(kioskId, last.id)
        }

        val stationId = utgService.findStationNode(kioskId).id
        context.nodeId = stationId

        return history
    }

    private fun clickMenu(menu: AutoOrderMenu, context: AutoOrderContext): ActionPathDto {
        logOrder(context.kioskId, context.taskId, "메뉴를 담습니다. 메뉴: ${menu.title}")
        val actionList = utgService.findMenuPath(context.kioskId, context.nodeId, menu.title)

        //메뉴를 담기 위해 메뉴 노드까지 이동후 필요한 만큼 클릭
        val last = actionList.last()
        for (act in actionList) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(act.x, act.y, act.title))
        }
        repeat(menu.count - 1) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(last.x, last.y, last.title))
        }

        return last
    }

    private fun clickOption(options: List<AutoOrderOption>, menuNode: ActionPathDto, context: AutoOrderContext) {
        for (opt in options) {
            logOrder(context.kioskId, context.taskId, "옵션을 선택합니다. 옵션: ${opt.title}")
            val dto = utgService.findOptionNode(context.kioskId, menuNode.id, opt.title)
            repeat(opt.count) {
                notificationService.sendActionCommand(context.kioskId, CoordinateDto(dto.x, dto.y, dto.title))
            }
        }
    }

    private fun clickBack(menuNode: ActionPathDto, context: AutoOrderContext) {
        val backList = utgService.findBackPath(context.kioskId, menuNode.id)
        for (back in backList) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(back.x, back.y, back.title))
        }
    }

    private fun proceedPayment(context: AutoOrderContext) {
        val actionList = utgService.findMenuPath(context.kioskId, context.nodeId, "complete").toMutableList()
        actionList.removeLast()

        for (act in actionList) {
            //포장/매장 클릭
            if (context.isPlace == false) {
                context.isPlace = clickPlace(context)
            }

            //결제 클릭
            clickPayment(act, context)
        }
    }

    private fun clickPayment(paymentNode: ActionPathDto, context: AutoOrderContext) {
        logOrder(context.kioskId, context.taskId, "결제를 진행중입니다. 현재: ${paymentNode.title}")
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(paymentNode.x, paymentNode.y, paymentNode.title))
    }

    private fun clickPlace(context: AutoOrderContext): Boolean {
        //현재 노드에서 인접한 노드에 포장/매장이 있는지 확인
        val kioskId = context.kioskId
        val taskId = context.taskId
        val action = utgService.findPlaceNode(kioskId, context.nodeId, context.place!!) ?: return false

        logOrder(kioskId, taskId,"포장/매장을 선택합니다. ${context.place}")
        notificationService.sendActionCommand(kioskId, CoordinateDto(action.x, action.y, action.title))
        return true
    }

    private fun logOrder(kioskId: String, taskId: String, message: String) {
        val logDto = LogDto(
            kioskId = kioskId,
            taskId = taskId,
            message = message
        )
        val json = jsonMapper.writeValueAsString(logDto)

        log.info(message)
        logService.sendLog(json)
    }
}