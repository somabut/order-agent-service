package com.orderagentservice.order.service

import com.orderagentservice.global.service.WordSimilarityService
import com.orderagentservice.order.util.UiExtractorManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UtgUpdateService @Autowired constructor(
    private val wordSimilarityService: WordSimilarityService,
    private val uiExtractorManager: UiExtractorManager,
    private val notificationService: NotificationService,
    private val utgService: UtgService
) {

}