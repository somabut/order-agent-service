package com.orderagentservice.order.service

import com.orderagentservice.global.model.dto.LogDto
import com.orderagentservice.logger
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.OrderResultDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.model.AutoOrderContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AutoOrderService @Autowired constructor(
    private val notificationService: NotificationService,
    private val utgService: UtgService,
    private val globalLogger: GlobalLogger
) {
    private val log = logger()

    fun proceed(kioskId: String, taskId: String, orderRequest: AutoOrderRequest) {
        logOrder(kioskId, taskId, "자동 주문을 시작합니다. 주문: ${orderRequest}")
        globalLogger.loggingOrderStart(kioskId, taskId)
        val startTime = System.nanoTime()

        //루트 노드 가져오기
        val nowNodeId = utgService.findRootNodeId(kioskId).id

        val context = AutoOrderContext(
            kioskId = kioskId,
            taskId = taskId,
            nodeId = nowNodeId,
            place = orderRequest.place,
            isPlace = (orderRequest.place == null)
        )

        //메뉴 담기
        val history = proceedMenu(
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

    private fun proceedMenu(context: AutoOrderContext, menuList: List<AutoOrderMenu>): List<OrderResultDto> {
        //메뉴 담기
        val kioskId = context.kioskId
        val taskId = context.taskId
        val history = mutableListOf<OrderResultDto>()
        for (menu in menuList) {
            //포장/매장 클릭
            val optHistory = mutableListOf<String>()
            if (context.isPlace == false) {
                context.isPlace = clickPlaceNode(context)
            }

            logOrder(kioskId, taskId, "메뉴를 담습니다. 메뉴: ${menu.title}")
            val actionList = utgService.findMenuPath(context.kioskId, context.nodeId, menu.title)

            //메뉴를 담기 위해 메뉴 노드까지 이동후 필요한 만큼 클릭
            val last = actionList.last()
            for (act in actionList) {
                notificationService.sendActionCommand(kioskId, CoordinateDto(act.x, act.y, act.title))
            }
            repeat(menu.count - 1) {
                notificationService.sendActionCommand(kioskId, CoordinateDto(last.x, last.y, last.title))
            }

            //옵션 선택
            for (opt in menu.autoOrderOptions) {
                logOrder(kioskId, taskId, "옵션을 선택합니다. 옵션: ${opt.title}")
                val dto = utgService.findOptionNode(kioskId, last.id, opt.title)
                repeat(opt.count) {
                    notificationService.sendActionCommand(kioskId, CoordinateDto(dto.x, dto.y, dto.title))
                }
                optHistory.add(opt.title)
            }
            history.add(
                OrderResultDto(
                    title = menu.title, category = menu.category,
                    options = optHistory, quantity = menu.count
                )
            )

            //메뉴 페이지로 돌아가기
            if (menu.autoOrderOptions.isNotEmpty()) {
                val backList = utgService.findBackPath(kioskId, last.id)
                for (back in backList) {
                    notificationService.sendActionCommand(kioskId, CoordinateDto(back.x, back.y, back.title))
                }
            }

            //현재 노드 갱신
            val categoryId = utgService.findCategoryNodeId(kioskId, last.id)
            context.nodeId = categoryId
        }
        return history
    }

    private fun proceedPayment(context: AutoOrderContext) {
        //결제는 complete 노드까지만 찾으면 됨
        val kioskId = context.kioskId
        val taskId = context.taskId
        val actionList = utgService.findMenuPath(kioskId, context.nodeId, "complete").toMutableList()
        actionList.removeLast()

        for (act in actionList) {
            //포장/매장 클릭
            if (context.isPlace == false) {
                context.isPlace = clickPlaceNode(context)
            }

            logOrder(kioskId, taskId, "결제를 진행중입니다. 현재: ${act.title}")
            notificationService.sendActionCommand(kioskId, CoordinateDto(act.x, act.y, act.title))
        }
    }

    private fun clickPlaceNode(context: AutoOrderContext): Boolean {
        //현재 노드에서 인접한 노드에 포장/매장이 있는지 확인
        val kioskId = context.kioskId
        val taskId = context.taskId
        val action = utgService.findPlaceNodeId(kioskId, context.nodeId, context.place!!) ?: return false

        logOrder(kioskId, taskId,"포장/매장을 선택합니다. ${context.place}")
        notificationService.sendActionCommand(kioskId, CoordinateDto(action.x, action.y, action.title))
        return true
    }

    private fun logOrder(kioskId: String, taskId: String, message: String) {
        val json = jsonMapper.writeValueAsString(
            LogDto(
                kioskId = kioskId,
                taskId = taskId,
                message = message
            )
        )
        log.info(message)
        notificationService.sendMessage(json)
    }
}