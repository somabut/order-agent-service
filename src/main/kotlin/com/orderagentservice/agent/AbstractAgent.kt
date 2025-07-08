package com.orderagentservice.agent

import com.orderagentservice.agent.util.LlmManager
import com.orderagentservice.jsonMapper

abstract class AbstractAgent (
    protected val llmManager: LlmManager
) {
    protected inline fun <reified T> determineWithType(vararg args: Any): T {
        val prompt = getPrompt(*args)
        val json = llmManager.queryGemini(prompt)
        return jsonMapper.readValue(json, T::class.java)
    }

    abstract fun getPrompt(vararg args: Any): String
}