package com.orderagentservice.order.service.auto

import com.orderagentservice.order.model.AutoOrderContext
import com.orderagentservice.order.model.dto.ActionPathDto
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.log.OrderProcessLog
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderOption
import com.orderagentservice.order.model.type.NodeType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.graph.ui.UiGraphService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AutoTaskExecutorImpl @Autowired constructor(
    private val graphService: UiGraphService,
    private val notificationService: NotificationService,
    private val orderLogSender: OrderLogSender
) : AutoTaskExecutor {
    override fun clickMenu(context: AutoOrderContext, menu: AutoOrderMenu): ActionPathDto {
        orderLogSender.logOrder(
            kioskId = context.kioskId, taskId = context.taskId,
            message = "메뉴를 담습니다. 메뉴: ${menu.title}",
            OrderProcessLog(
                kioskId = context.kioskId, nodeType = NodeType.MENU, title = menu.title
            )
        )
        val actionList = graphService.findPath(context.kioskId, context.nodeId, menu.title)

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

    override fun clickOption(context: AutoOrderContext, options: List<AutoOrderOption>, menuNodeId: String): String {
        val optionHistory = mutableListOf<String>()
        var nodeId = menuNodeId
        for (opt in options) {
            orderLogSender.logOrder(
                kioskId = context.kioskId, taskId = context.taskId,
                message = "옵션을 선택합니다. 옵션: ${opt.title}",
                OrderProcessLog(
                    kioskId = context.kioskId, nodeType = NodeType.OPTION, title = opt.title
                )
            )

            val actionList = graphService.findPath(context.kioskId, nodeId, opt.title)
            for (act in actionList) {
                val coordinate = notificationService.sendActionCommand(context.kioskId, CoordinateDto(act.x, act.y, act.title))
                optionHistory.add(coordinate.title)
            }
            if (actionList.size > 1) nodeId = actionList[actionList.lastIndex - 1].id
        }

        return nodeId
    }

    override fun clickBack(context: AutoOrderContext, menuNodeId: String): String {
        val backList = graphService.findPath(context.kioskId, menuNodeId, "station")

        for (back in backList) {
            notificationService.sendActionCommand(context.kioskId, CoordinateDto(back.x, back.y, back.title))
        }

        val lastNode = backList.last().id
        return lastNode
    }

    override fun clickPayment(context: AutoOrderContext, paymentNode: ActionPathDto) {
        orderLogSender.logOrder(
            kioskId = context.kioskId, taskId = context.taskId,
            message = "결제를 진행중입니다. 현재: ${paymentNode.title}",
            OrderProcessLog(
                kioskId = context.kioskId, nodeType = NodeType.PAYMENT, title = paymentNode.title
            )
        )
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(paymentNode.x, paymentNode.y, paymentNode.title))
    }

    override fun clickPlace(context: AutoOrderContext): Boolean {
        //현재 노드에서 인접한 노드에 포장/매장이 있는지 확인
        val kioskId = context.kioskId
        val taskId = context.taskId
        val action = graphService.findPlace(kioskId, context.nodeId, context.place!!) ?: return false

        orderLogSender.logOrder(
            kioskId = kioskId, taskId = taskId,
            message = "포장/매장을 선택합니다. ${context.place}",
            OrderProcessLog(
                kioskId = kioskId, nodeType = NodeType.PAYMENT, title = context.place
            )
        )
        val coordinate = notificationService.sendActionCommand(kioskId, CoordinateDto(action.x, action.y, action.title))
        context.history.payment = coordinate.title
        return true
    }
}