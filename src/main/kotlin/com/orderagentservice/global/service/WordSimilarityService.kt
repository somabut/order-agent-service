package com.orderagentservice.global.service

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import jakarta.annotation.PreDestroy
import jep.JepConfig
import jep.SharedInterpreter
import org.springframework.stereotype.Service

@Service
class WordSimilarityService {
    private val jep: SharedInterpreter by lazy {
        val config = JepConfig()
            .addIncludePaths("/venv/lib/python3.11/site-packages")
        SharedInterpreter.setConfig(config)

        SharedInterpreter().apply {
            eval("import sys")
            eval("import numpy")
            eval("import sklearn")

            eval("sys.path.append('.')")
            eval("sys.path.append('/')")
            eval("from word_compare import KoreanSimilarityCalculator")
            eval("calculator = KoreanSimilarityCalculator()")
        }
    }

    fun findBestMatch(targetWord: String, uiList: List<LlmUiComponentDto>): WordMatchDto {
        val candidates = uiList.map { listOf(it.x, it.y, it.title) }

        jep.set("candidates", candidates)
        jep.eval("result = calculator.find_best_match('$targetWord', candidates)")

        val resultMap = jep.getValue("result", Map::class.java) as Map<String, Any>

        val x = (resultMap["x"] as Number).toInt()
        val y = (resultMap["y"] as Number).toInt()
        val word = resultMap["word"] as String
        val score = (resultMap["score"] as Number).toDouble()

        return WordMatchDto(x, y, word, score)
    }

    @PreDestroy
    fun cleanup() {
        jep.close()
    }
}