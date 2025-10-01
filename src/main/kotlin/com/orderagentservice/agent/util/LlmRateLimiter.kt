package com.orderagentservice.agent.util

import com.orderagentservice.agent.exception.LlmServerOverLoadException
import com.orderagentservice.agent.model.LlmProvider
import com.orderagentservice.agent.model.response.ClaudResponse
import com.orderagentservice.agent.model.response.GptErrorResponse
import com.orderagentservice.jsonMapper
import com.orderagentservice.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class LlmRateLimiter @Autowired constructor(
    private val env: Environment
) {
    private val log = logger()

    private val CLAUD_API_KEY = env.getProperty("agent.claud.api-key")!!

    fun <T> executeWithLimit(provider: LlmProvider, block: (apiKey: String) -> T): T {
        return when (provider) {
            LlmProvider.CLAUD -> executeForClaud(block)
        }
    }

    private fun <T> executeForClaud(block: (apiKey: String) -> T): T {
        var waitTime = 2L
        val maxWaitTime = 16L

        //2배씩 늘리며 기다리기
        while (true) {
            var response: T
            try {
                response = block(CLAUD_API_KEY)
            } catch (e: HttpServerErrorException) {
                if (e.statusCode.value() != 529) {
                    throw e
                }
                if (waitTime > maxWaitTime) {
                    throw LlmServerOverLoadException()
                }

                log.info("엔트로픽 서버 과부화로 인해 ${waitTime}초 대기합니다.")
                Thread.sleep(waitTime * 1000)
                waitTime *= 2
                continue
            }
            return response
        }
    }
}