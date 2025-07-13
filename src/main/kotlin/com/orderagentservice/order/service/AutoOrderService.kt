package com.orderagentservice.order.service

import com.orderagentservice.logger
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.dto.OrderResultDto
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderRequest
import com.orderagentservice.order.util.GlobalLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AutoOrderService @Autowired constructor(
    private val notificationService: NotificationService,
    private val utgService: UtgService,
    private val globalLogger: GlobalLogger
) {
    private val log = logger()

    //TODO(시작 노드 아이디 가져오기. 밑의 root는 임시코드)
    var nowNodeId = "root"
    var isPlace = false

    fun proceed(kioskId: String, orderRequest: AutoOrderRequest) {
        log.info("자동 주문을 시작합니다. 주문: ${orderRequest}")
        globalLogger.loggingOrderStart(kioskId)
        val startTime = System.nanoTime()
        isPlace = (orderRequest.place == null)

        //메뉴 담기
        val history = proceedMenu(kioskId, orderRequest.autoOrderMenus, orderRequest.place)

        //결제하기
        proceedPayment(kioskId, orderRequest.place)

        val endTime = System.nanoTime()
        val processingTime = (endTime - startTime) / 1000000
        log.info("자동 주문 완료. 수행시간: ${processingTime}ms")
        globalLogger.loggingOrderResult(kioskId, history, processingTime, orderRequest.payment)
    }

    private fun proceedMenu(kioskId: String, menuList: List<AutoOrderMenu>, place: String?): List<OrderResultDto> {
        //메뉴 담기
        val history = mutableListOf<OrderResultDto>()
        for (menu in menuList) {
            //포장/매장 클릭
            val optHistory = mutableListOf<String>()
            if (isPlace == false) {
                isPlace = clickPlaceNode(kioskId, nowNodeId, place!!)
            }

            log.info("메뉴를 담습니다. 메뉴: ${menu.title}")
            val actionList = utgService.findMenuPath(kioskId, nowNodeId, menu.title)

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
                log.info("옵션을 선택합니다. 옵션: ${opt.title}")
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
            nowNodeId = categoryId
        }
        return history
    }

    private fun proceedPayment(kioskId: String, place: String?) {
        //결제는 complete 노드까지만 찾으면 됨
        val actionList = utgService.findMenuPath(kioskId, nowNodeId, "complete").toMutableList()
        actionList.removeLast()

        for (act in actionList) {
            //포장/매장 클릭
            if (isPlace == false) {
                isPlace = clickPlaceNode(kioskId, nowNodeId, place!!)
            }

            log.info("결제를 진행중입니다. 현재: ${act.title}")
            notificationService.sendActionCommand(kioskId, CoordinateDto(act.x, act.y, act.title))
        }
    }

    private fun clickPlaceNode(kioskId: String, nodeId: String, place: String): Boolean {
        //현재 노드에서 인접한 노드에 포장/매장이 있는지 확인
        val action = utgService.findPlaceNodeId(kioskId, nodeId, place) ?: return false

        log.info("포장/매장을 선택합니다. ${place}")
        notificationService.sendActionCommand(kioskId, CoordinateDto(action.x, action.y, action.title))
        return true
    }
}