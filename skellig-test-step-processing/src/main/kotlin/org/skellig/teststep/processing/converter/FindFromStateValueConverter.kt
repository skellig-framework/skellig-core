package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.util.regex.Pattern

class FindFromStateValueConverter(val testScenarioState: TestScenarioState,
                                  val valueExtractor: TestStepValueExtractor?) : TestStepValueConverter {

    companion object {
        private val FIND_PATTERN = Pattern.compile("find\\(([\\w_\$./)(]+)\\)")
    }

    override fun convert(value: String?): Any? {
        return value?.let {
            val matcher = FIND_PATTERN.matcher(it)
            var result: Any? = it
            if (matcher.find()) {
                val extractPath = matcher.group(1)
                result = testScenarioState.reversed()
                        .mapNotNull { e -> tryExtract(e, extractPath) }
                        .firstOrNull()
                        ?: throw(TestValueConversionException("Could not find data in the current state by '$extractPath' path"))
                if (result is String) {
                    return convert(value.replace("find($extractPath)", result.toString()))
                }
            }
            return result
        }
    }

    private fun tryExtract(e: Pair<String, Any?>, extractPath: String?): Any? {
        return try {
            valueExtractor?.extract(e.second, extractPath)
        } catch (ex: Exception) {
            null
        }
    }
}