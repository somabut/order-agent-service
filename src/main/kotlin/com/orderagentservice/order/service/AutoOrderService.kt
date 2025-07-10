package com.orderagentservice.order.service

import com.orderagentservice.logger
import com.orderagentservice.order.model.request.AutoOrderMenu
import com.orderagentservice.order.model.request.AutoOrderRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AutoOrderService @Autowired constructor(
    private val notificationService: NotificationService,
    private val uiGraphService: UiGraphService
) {
    private val log = logger()

    //TODO(시작 노드 아이디 가져오기. 밑의 root는 임시코드)
    var nowNodeId = "root"

    fun proceed(kioskId: String, orderRequest: AutoOrderRequest) {
        log.info("자동 주문을 시작합니다. 주문: ${orderRequest}")
        val startTime = System.nanoTime()

        //메뉴 담기
        proceedMenu(kioskId, orderRequest.autoOrderMenus)

        //결제하기
        proceedPayment(kioskId)

        val endTime = System.nanoTime()
        log.info("자동 주문 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    private fun proceedMenu(kioskId: String, menuList: List<AutoOrderMenu>) {
        //메뉴 담기
        for (menu in menuList) {
            log.info("메뉴를 담습니다. 메뉴: ${menu.title}")
            val actionList = uiGraphService.findPathToPath(kioskId, nowNodeId, menu.title)

            //메뉴를 담기 위해 메뉴 노드까지 이동후 필요한 만큼 클릭
            val last = actionList.last()
            for (act in actionList) {
                notificationService.sendActionCommand(kioskId, listOf(act.x, act.y))
            }
            repeat(menu.count - 1) {
                notificationService.sendActionCommand(kioskId, listOf(last.x, last.y))
            }

            //옵션 선택
            for (opt in menu.autoOrderOptions) {
                log.info("옵션을 선택합니다. 옵션: ${opt.title}")
                val dto = uiGraphService.findOptionNode(kioskId, last.id, opt.title)
                repeat(opt.count) {
                    notificationService.sendActionCommand(kioskId, listOf(dto.x, dto.y))
                }
            }

            //메뉴 페이지로 돌아가기
            if (menu.autoOrderOptions.isNotEmpty()) {
                val backList = uiGraphService.findBackToPath(kioskId, last.id)
                for (back in backList) {
                    notificationService.sendActionCommand(kioskId, listOf(back.x, back.y))
                }
            }

            //현재 노드 갱신
            val categoryId = uiGraphService.findCategoryNodeId(kioskId, last.id)
            nowNodeId = categoryId
        }
    }

    private fun proceedPayment(kioskId: String) {
        //결제는 complete 노드까지만 찾으면 됨
        val actionList = uiGraphService.findPathToPath(kioskId, nowNodeId, "complete").toMutableList()
        actionList.removeLast()

        for (act in actionList) {
            log.info("결제를 진행중입니다. 현재: ${act.title}")
            notificationService.sendActionCommand(kioskId, listOf(act.x, act.y))
        }
    }
}