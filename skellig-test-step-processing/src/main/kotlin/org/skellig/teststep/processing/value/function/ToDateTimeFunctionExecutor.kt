package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import org.skellig.teststep.processing.exception.TestValueConversionException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery

class ToDateTimeFunctionExecutor : FunctionValueExecutor {

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    }

    override fun execute(name: String, args: Array<Any?>): Any? {
        return if (args.size == 1) {
            val value = args[0]?.toString()
            parseDate(value) { LocalDateTime.from(it) }
        } else {
            throw FunctionValueExecutionException("Function `$name` can only accept 1 argument. Found ${args.size}")
        }
    }

    private fun parseDate(value: String?, query: TemporalQuery<*>) =
        try {
            DATE_TIME_FORMATTER.parse(value, query)
        } catch (ex: Exception) {
            throw TestValueConversionException("Failed to convert date $value by pattern $DATE_TIME_FORMATTER");
        }

    override fun getFunctionName(): String = "toDateTime"
}