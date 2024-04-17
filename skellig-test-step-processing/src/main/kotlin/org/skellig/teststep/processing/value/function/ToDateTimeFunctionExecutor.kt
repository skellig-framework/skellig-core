package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery

/**
 * Abstract class representing an executor for functions that convert a value to a [LocalDate] or [LocalDateTime].
 *
 * This class is an implementation of the [FunctionValueExecutor] interface and provides a common base for other
 * function executors that convert values to dates or date-times. It handles the parsing of input values and the
 * application of a date/time pattern and time zone.
 *
 * @throws FunctionExecutionException if the function does not accept the correct number of arguments, or if a
 * mandatory date/time pattern is not provided
 */
abstract class BaseToDateTimeFunctionExecutor : FunctionValueExecutor {

    companion object {
        internal val UTC = ZoneId.of("UTC")
    }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        return if (args.isNotEmpty()) {
            when (value) {
                is String -> {
                    var timezone: String? = null
                    val pattern = args[0]?.toString()?.trim()
                    if (args.size == 2) {
                        timezone = args[1]?.toString()?.trim()
                    }
                    pattern?.let {
                        parseDate(value, timezone, pattern, getTemporalQuery())
                    } ?: throw FunctionExecutionException("Date/Time pattern is mandatory for '${getFunctionName()}' function")
                }
                else -> value
            }
        } else throw FunctionExecutionException("Function `${getFunctionName()}` can only accept 1 or 2 String arguments. Found ${args.size}")
    }

    protected abstract fun getTemporalQuery(): TemporalQuery<*>;

    private fun parseDate(value: String?, timezone: String?, pattern: String, query: TemporalQuery<*>) =
        try {
            val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
                .withZone(timezone?.let { ZoneId.of(timezone) } ?: UTC)
            dateTimeFormatter.parse(value, query)
        } catch (ex: Exception) {
            throw FunctionExecutionException("Failed to convert date $value by pattern $pattern");
        }
}

/**
 * Executes the "toDateTime" function, which converts a value to a [LocalDateTime] object based on the provided date/time pattern and time zone.
 *
 * Supported args:
 * - toDateTime(`<date time pattern>`) - for example: toDateTime("dd/MM/yyyy HH:mm")
 * - toDateTime(`<date time pattern>`, `<time zone>`) - for example: toDateTime("dd/MM/yyyy HH:mm", "+15")
 */
class ToDateTimeFunctionExecutor : BaseToDateTimeFunctionExecutor() {

    override fun getTemporalQuery(): TemporalQuery<*> {
        return TemporalQuery { LocalDateTime.ofInstant(Instant.from(it), UTC) }
    }

    override fun getFunctionName(): String = "toDateTime"
}

/**
 * Executes the "toDate" function, which converts a value to a [LocalDate] object based on the provided date pattern.
 *
 * Supported args:
 * - toDate(`<date time pattern>`) - for example: toDateTime("dd/MM/yyyy HH:mm")
 */
class ToDateFunctionExecutor : BaseToDateTimeFunctionExecutor() {

    override fun getTemporalQuery(): TemporalQuery<*> {
        return TemporalQuery { LocalDate.from(it) }
    }

    override fun getFunctionName(): String = "toDate"
}
