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

    fun proceed(kioskId: String, orderRequest: AutoOrderRequest) {
        log.info("자동 주문을 시작합니다. 주문: ${orderRequest}")
        val startTime = System.nanoTime()

        //메뉴 담기
        proceedMenu(kioskId, orderRequest.autoOrderMenus)

        //TODO(결제하기)


        val endTime = System.nanoTime()
        log.info("자동 주문 완료. 수행시간: ${(endTime - startTime) / 1000000}ms")
    }

    private fun proceedMenu(kioskId: String, menuList: List<AutoOrderMenu>) {
        //메뉴 담기
        //TODO(시작 노드 아이디 가져오기)
        var nowId = "root"
        for (menu in menuList) {
            log.info("메뉴를 담습니다. 메뉴: ${menu.title}")
            val actionList = uiGraphService.findTargetPath(kioskId, nowId, menu.title)

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
                val dto = uiGraphService.findOptTarget(kioskId, last.id, opt.title)
                repeat(opt.count) {
                    notificationService.sendActionCommand(kioskId, listOf(dto.x, dto.y))
                }
            }

            //메뉴 페이지로 돌아가기
            if (menu.autoOrderOptions.isNotEmpty()) {
                val backList = uiGraphService.findBackPath(kioskId, last.id)
                for (back in backList) {
                    notificationService.sendActionCommand(kioskId, listOf(back.x, back.y))
                }
            }

            //현재 노드 갱신
            val categoryId = uiGraphService.findCategoryNodeId(kioskId, last.id)
            nowId = categoryId
        }
    }

    private fun proceedPayment() {

    }
}
//현재 노드는 메뉴가 아니라 카테고리여야함
//일단 스텝단위로 뽑기

//한 스텝에서 필요한 액션하기
//클릭 횟수, 클릭해야 하는 좌표등

//애초에 주문도 주문서 느낌으로 json으로 주면 좋음