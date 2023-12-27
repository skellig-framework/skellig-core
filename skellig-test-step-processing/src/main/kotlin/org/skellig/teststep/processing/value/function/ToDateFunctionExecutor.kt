package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery

class ToDateFunctionExecutor : FunctionValueExecutor {
    companion object {
        private const val DATE_PATTERN = "dd-MM-yyyy"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN)
    }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        val argValue =
            value?.toString() ?: if (args.size == 1) {
                args[0]?.toString()
            } else throw FunctionExecutionException("Function `$name` can only accept 1 argument. Found ${args.size}")
        return parseDate(argValue) { LocalDate.from(it) }
    }

    private fun parseDate(value: String?, query: TemporalQuery<*>) =
        try {
            DATE_FORMATTER.parse(value, query)
        } catch (ex: Exception) {
            throw FunctionExecutionException("Failed to convert date $value by pattern $DATE_PATTERN");
        }

    override fun getFunctionName(): String = "toDate"
}