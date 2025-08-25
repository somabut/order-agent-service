package com.orderagentservice.agent

import com.orderagentservice.agent.model.UsageTracker
import com.orderagentservice.agent.util.LlmManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UsageTrackerTest @Autowired constructor(
    private val usageTracker: UsageTracker,
    private val llmManager: LlmManager
) {
    @Test
    fun `토큰 사용량이 추척된다`() {
        val response1 = llmManager.query("frank ocean의 blonde는 왜 PBR&B 명반이지?")
        println(response1)
        println(usageTracker.totalUsage)

        val response2 = llmManager.query("kanyewest의 yeezus는 다른 아티스트에게 어떤 영향을 끼쳤지?")
        println(response2)
        println(usageTracker.totalUsage)
    }
}