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

    override fun convert(value: Any?): Any? =
            when (value) {
                is String -> {
                    val matcher = FIND_PATTERN.matcher(value.toString())
                    var result: Any? = value
                    if (matcher.find()) {
                        val extractPath = matcher.group(1)
                        result = testScenarioState.reversed()
                                .mapNotNull { e -> tryExtract(e, extractPath) }
                                .firstOrNull()
                                ?: throw(TestValueConversionException("Could not find data in the current state by '$extractPath' path"))
                        if (result is String) convert(value.toString().replace("find($extractPath)", result.toString()))
                        else result
                    } else result
                }
                else -> value
            }

    private fun tryExtract(e: Pair<String, Any?>, extractPath: String?): Any? {
        return try {
            valueExtractor?.extract(e.second, extractPath)
        } catch (ex: Exception) {
            null
        }
    }
}