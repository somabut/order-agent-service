package com.orderagentservice.global.service

import com.orderagentservice.agent.model.dto.LlmUiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import jakarta.annotation.PreDestroy
import jep.JepConfig
import jep.SharedInterpreter
import org.springframework.stereotype.Service

@Service
class WordSimilarityService {
    fun findBestMatch(targetWord: String, uiList: List<LlmUiComponentDto>): WordMatchDto {
        val config = JepConfig()
            .addIncludePaths("/venv/lib/python3.11/site-packages")
            .addIncludePaths("/")
        SharedInterpreter.setConfig(config)

        SharedInterpreter().use { jep ->
            jep.eval("import sys")
            jep.eval("sys.path.append('/')")
            jep.eval("import numpy")
            jep.eval("import sklearn")
            jep.eval("from word_compare import KoreanSimilarityCalculator")
            jep.eval("calculator = KoreanSimilarityCalculator()")

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
    }
}