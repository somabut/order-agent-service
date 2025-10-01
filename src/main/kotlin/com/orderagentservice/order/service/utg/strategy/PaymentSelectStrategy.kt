package com.orderagentservice.order.service.utg.strategy

import com.orderagentservice.agent.PaymentAgent
import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.order.model.UtgContext
import com.orderagentservice.order.model.dto.CoordinateDto
import com.orderagentservice.order.model.type.StrategyType
import com.orderagentservice.order.service.NotificationService
import com.orderagentservice.order.service.utg.payment.PaymentNodeIntegrator
import com.orderagentservice.order.service.utg.place.PlaceUtgService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

abstract class PaymentSelectStrategy {
    protected abstract val paymentAgent: PaymentAgent
    protected abstract val paymentNodeIntegrator: PaymentNodeIntegrator
    protected abstract val notificationService: NotificationService

    abstract fun execute(context: UtgContext, uiList: List<UiComponentDto>): Boolean

    protected fun setUpNode(
        context: UtgContext,
        uiList: List<UiComponentDto>
    ): Boolean {
        val action = paymentAgent.determineAction(uiList)
        if (action.goNext == false) return false

        //노드 저장
        paymentNodeIntegrator.integratePaymentNode(action, context)

        //클릭 액션 요청
        notificationService.sendActionCommand(context.kioskId, CoordinateDto(x = action.coordinate[0], y = action.coordinate[1], title = action.title))

        return true
    }
}

@Component(StrategyType.IN_PAYMENT_PLACE)
class PlacePaymentSelectStrategy @Autowired constructor(
    override val paymentAgent: PaymentAgent,
    override val paymentNodeIntegrator: PaymentNodeIntegrator,
    override val notificationService: NotificationService,
    private val placeUtgService: PlaceUtgService
) : PaymentSelectStrategy() {
    override fun execute(
        context: UtgContext,
        uiList: List<UiComponentDto>
    ): Boolean {
        if (context.isPlaceDetermined == false) {
            placeUtgService.initializeGraph(context, uiList)
        }

        return setUpNode(
            context = context,
            uiList = uiList
        )
    }
}

@Component(StrategyType.EX_PAYMENT_PLACE)
class DefaultPaymentSelectStrategy @Autowired constructor(
    override val paymentAgent: PaymentAgent,
    override val paymentNodeIntegrator: PaymentNodeIntegrator,
    override val notificationService: NotificationService,
) : PaymentSelectStrategy() {
    override fun execute(
        context: UtgContext,
        uiList: List<UiComponentDto>
    ): Boolean {
        return setUpNode(
            context = context,
            uiList = uiList
        )
    }
}