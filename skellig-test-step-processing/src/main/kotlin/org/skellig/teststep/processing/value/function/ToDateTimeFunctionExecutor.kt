package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery

class ToDateTimeFunctionExecutor : FunctionValueExecutor {

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        val argValue =
            value?.toString() ?: if (args.size == 1) {
                args[0]?.toString()
            } else throw FunctionExecutionException("Function `$name` can only accept 1 argument. Found ${args.size}")
        return parseDate(argValue) { LocalDateTime.from(it) }
    }

    private fun parseDate(value: String?, query: TemporalQuery<*>) =
        try {
            DATE_TIME_FORMATTER.parse(value, query)
        } catch (ex: Exception) {
            throw FunctionExecutionException("Failed to convert date $value by pattern $DATE_TIME_FORMATTER");
        }

    override fun getFunctionName(): String = "toDateTime"
}