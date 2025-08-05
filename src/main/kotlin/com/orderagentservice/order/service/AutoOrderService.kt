package com.orderagentservice.order.service

import com.orderagentservice.global.model.dto.LogDto
import com.orderagentservice.global.service.LogService
import com.orderagentservice.logger
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.global.util.GlobalLogger
import com.orderagentservice.jsonMapper
import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.AutoOrderResultDto
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.MenuInfoDto
import com.orderagentservice.order.model.request.AutoOrderOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AutoOrderService @Autowired constructor(
    private val notificationService: NotificationService,
    private val logService: LogService,
    private val graphService: GraphService,
    private val globalLogger: GlobalLogger
) {
    private val log = logger()

    fun order(kioskId: String, taskId: String, orderRequest: AutoOrderRequest): AutoOrderResultDto {
        val requestJson = jsonMapper.writeValueAsString(orderRequest)
        logOrder(kioskId, taskId, "자동 주문을 시작합니다. 주문: ${requestJson}")
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

        logOrder(kioskId, taskId, "자동 주문 완료. 수행시간: ${processingTime}ms")
        globalLogger.loggingOrderResult(
            kioskId = kioskId, menuList = context.history,
            processingTime = processingTime, paymentMethod = orderRequest.payment,
            taskId = taskId
        )

        return context.history
    }

    private fun putMenus(context: AutoOrderContext, menuList: List<AutoOrderMenu>) {
        val kioskId = context.kioskId
        for (menu in menuList) {
            //포장/매장 클릭
            if (context.isPlaceSelected == false) {
                context.isPlaceSelected = clickPlace(context)
            }

            //메뉴 클릭
            val lastMenu = clickMenu(menu, context)

            //옵션 클릭
            val optHistory = clickOption(menu.autoOrderOptions, lastMenu, context)

            //히스토리에 추가
            val historyMenu = MenuInfoDto(
                title = menu.title,
                options = optHistory,
                category = menu.category
            )
            context.history.menus.add(historyMenu)

            //돌아가는 UI 클릭
            clickBack(lastMenu, context)

            //현재 노드 갱신
            context.nodeId = graphService.findCategoryNodeId(kioskId, lastMenu.id)
        }

        val stationId = graphService.findStation(kioskId).id
        context.nodeId = stationId
    }

    private fun clickMenu(menu: AutoOrderMenu, context: AutoOrderContext): ActionPathDto {
        logOrder(context.kioskId, context.taskId, "메뉴를 담습니다. 메뉴: ${menu.title}")
        val actionList = graphService.findMenuPath(context.kioskId, context.nodeId, menu.title)

        //메뉴를 담기 위해 메뉴 노드까지 이동후 필요한 만큼 클릭
        val lastMenu = actionList.last()
        for (act in actionList) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(act.x, act.y, act.title))
        }
        repeat(menu.count - 1) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(lastMenu.x, lastMenu.y, lastMenu.title))
        }

        return lastMenu
    }

    private fun clickOption(options: List<AutoOrderOption>, menuNode: ActionPathDto, context: AutoOrderContext): List<String> {
        val optionHistory = mutableListOf<String>()
        for (opt in options) {
            logOrder(context.kioskId, context.taskId, "옵션을 선택합니다. 옵션: ${opt.title}")
            val dto = graphService.findOption(context.kioskId, menuNode.id, opt.title)
            repeat(opt.count) {
                val coordinate = notificationService.sendActionCommand(context.kioskId, CoordinateDto(dto.x, dto.y, dto.title))
                optionHistory.add(coordinate.title)
            }
        }

        return optionHistory
    }

    private fun clickBack(menuNode: ActionPathDto, context: AutoOrderContext) {
        val backList = graphService.findBackPath(context.kioskId, menuNode.id).toMutableList()
        backList.removeLast()

        for (back in backList) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(back.x, back.y, back.title))
        }
    }

    private fun proceedPayment(context: AutoOrderContext) {
        val actionList = graphService.findPaymentPath(context.kioskId, context.nodeId).toMutableList()
        actionList.removeLast()

        for (act in actionList) {
            //포장/매장 클릭
            if (context.isPlaceSelected == false) {
                context.isPlaceSelected = clickPlace(context)
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
        val action = graphService.findPlace(kioskId, context.nodeId, context.place!!) ?: return false

        logOrder(kioskId, taskId,"포장/매장을 선택합니다. ${context.place}")
        val coordinate = notificationService.sendActionCommand(kioskId, CoordinateDto(action.x, action.y, action.title))
        context.history.payment = coordinate.title
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