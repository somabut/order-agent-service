package com.orderagentservice.order.utg.service

import com.orderagentservice.agent.model.dto.UiComponentDto
import com.orderagentservice.global.model.dto.WordMatchDto
import jep.JepConfig
import jep.SharedInterpreter
import org.springframework.stereotype.Service

@Service
class WordSimilarityService {
    init {
        val config = JepConfig()
            .addIncludePaths("/venv/lib/python3.11/site-packages")
            .addIncludePaths("/")
        SharedInterpreter.setConfig(config)
    }

    private fun <T> load(block: (SharedInterpreter) -> T): T {
        return SharedInterpreter().use { jep ->
            jep.eval("import sys")
            jep.eval("sys.path.append('/')")
            jep.eval("import numpy")
            jep.eval("import sklearn")
            jep.eval("from word_compare import KoreanSimilarityCalculator")
            jep.eval("calculator = KoreanSimilarityCalculator()")
            block(jep)
        }
    }

    fun findBestMatch(targetWord: String, uiList: List<UiComponentDto>): WordMatchDto {
        return load { jep ->
            val candidates = uiList.map { listOf(it.x, it.y, it.title) }
            jep.set("candidates", candidates)
            jep.eval("result = calculator.find_best_match('$targetWord', candidates)")

            val resultMap = jep.getValue("result", Map::class.java) as Map<String, Any>
            val x = (resultMap["x"] as Number).toInt()
            val y = (resultMap["y"] as Number).toInt()
            val word = resultMap["word"] as String
            val score = (resultMap["score"] as Number).toDouble()

            WordMatchDto(x, y, word, score)
        }
    }

    fun determinePage(sourceList: List<String>, uiList: List<UiComponentDto>): Boolean {
        return load { jep ->
            val pageList = uiList.map { it.title }
            jep.set("need_list", sourceList)
            jep.set("page_list", pageList)
            jep.eval("result = calculator.determine_page(need_list, page_list)")

            val result = (jep.getValue("result", Any::class.java) as Number).toInt()
            result != 0
        }
    }
}