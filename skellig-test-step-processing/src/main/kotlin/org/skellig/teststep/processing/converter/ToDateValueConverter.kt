package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.experiment.FunctionValueProcessor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery
import java.util.regex.Pattern

class ToDateValueConverter : FunctionValueProcessor {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    }

    override fun execute(name: String, args: Array<Any?>): Any? {
        return if (args.size == 1) {
            val value = args[0]?.toString()
            parseDate(value, DATE_FORMATTER) { LocalDate.from(it) }
        } else {
            throw TestDataConversionException("Function `$name` can only accept 1 argument. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "toDate"

    private fun parseDate(value: String?, formatter: DateTimeFormatter, query: TemporalQuery<*>) =
        try {
            formatter.parse(value, query)
        } catch (ex: Exception) {
            throw TestValueConversionException("Failed to convert date $value by pattern $formatter");
        }
}