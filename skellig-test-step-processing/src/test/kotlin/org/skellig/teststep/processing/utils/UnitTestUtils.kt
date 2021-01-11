package org.skellig.teststep.processing.utils

import org.skellig.teststep.processing.model.ExpectedResult

sealed class UnitTestUtils {

    companion object {
        fun createMap(vararg params: Any?): Map<String, Any?> {
            val map: MutableMap<String, Any?> = HashMap()
            var i = 0
            while (i < params.size) {
                map[params[i] as String] = params[i + 1]
                i += 2
            }
            return map
        }

        fun extractExpectedValue(expectedResult: ExpectedResult, vararg indexPath: Int): ExpectedResult {
            var newExpectedResult = expectedResult
            for (index in indexPath) {
                newExpectedResult = newExpectedResult.get<List<ExpectedResult>>()!![index]
            }
            return newExpectedResult
        }
    }
}