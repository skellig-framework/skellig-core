package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.TestValueConversionException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery

class ToDateFunctionExecutor : FunctionValueExecutor {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    }

    override fun execute(name: String, args: Array<Any?>): Any? {
        return if (args.size == 1) {
            val value = args[0]?.toString()
            parseDate(value) { LocalDate.from(it) }
        } else {
            throw TestDataConversionException("Function `$name` can only accept 1 argument. Found ${args.size}")
        }
    }

    private fun parseDate(value: String?, query: TemporalQuery<*>) =
        try {
            DATE_FORMATTER.parse(value, query)
        } catch (ex: Exception) {
            throw TestValueConversionException("Failed to convert date $value by pattern $DATE_FORMATTER");
        }

    override fun getFunctionName(): String = "toDate"
}