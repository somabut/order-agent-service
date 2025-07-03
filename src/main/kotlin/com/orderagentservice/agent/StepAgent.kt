package com.orderagentservice.agent

import com.fasterxml.jackson.module.kotlin.readValue
import com.orderagentservice.jsonMapper
import com.orderagentservice.agent.model.dto.AgentStepDto
import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StepAgent @Autowired constructor(
    private val llmManager: LlmManager
) {
    private val log = logger()

    fun cutStep(input: String): AgentStepDto {
        val prompt = getPrompt(input)
        val json = llmManager.queryGeminiModel(prompt)
        val steps: AgentStepDto = jsonMapper.readValue<AgentStepDto>(json)
        return steps
    }

    private fun getPrompt(input: String): String {
        val prompt = """
            You are an order expert who sees the food orders given and divides them into steps.
            The response should be in Json format.

            Here is order request input: ${input}

            IMPORTANT NOTES:
            1. The types of steps consist of menu(different types may come in), '매장'/'포장', and payment.
            2. If the order reqeust has multiple items of the same menu, it's done in one step.

            One Example(input: 아메리카노 3잔, 케이크 2개, 빅맥3개 포장 카드 결제해줘)
            ```json
            {
                "steps" = ["아메리카노 3잔", "케이크 2개", "빅맥3개", "포장", "카드 결제해줘"]
            }
            ```

            Another Example(input: 크피스퍼 클래식, 콜라 3잔, 핫 토마토 모짜볼 2개 카드 결제해줘)
            ```json
            {
                "steps" = ["크피스퍼 클래식", "콜라 3잔", "핫 토마토 모짜볼 2개", "카드 결제해줘"]
            }
            ```
        """.trimIndent()

        return prompt
    }
}