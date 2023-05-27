package org.skellig.teststep.processing.value.extractor

import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import org.skellig.teststep.processing.value.extractor.exception.ValueExtractionException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery

abstract class BaseToDateTimeValueExtractor : ValueExtractor {

    companion object {
        internal val UTC = ZoneId.of("UTC")
    }

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return if (args.isNotEmpty()) {
            when (value) {
                is String -> {
                    var timezone: String? = null
                    val pattern = args[0]?.toString()?.trim()
                    if (args.size == 2) {
                        timezone = args[1]?.toString()?.trim()
                    }
                    pattern?.let {
                        val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
                            .withZone(timezone?.let { ZoneId.of(timezone) } ?: UTC)
                        parseDate(value, dateTimeFormatter, getTemporalQuery())
                    } ?: throw ValueExtractionException("Date/Time pattern is mandatory for '${getExtractFunctionName()}' function")
                }
                else -> value
            }
        } else throw FunctionValueExecutionException("Function `${getExtractFunctionName()}` can only accept 1 or 2 String arguments. Found ${args.size}")
    }

    protected abstract fun getTemporalQuery(): TemporalQuery<*>;

    private fun parseDate(value: String?, formatter: DateTimeFormatter, query: TemporalQuery<*>) =
        try {
            formatter.parse(value, query)
        } catch (ex: Exception) {
            throw ValueExtractionException("Failed to convert date $value by pattern $formatter");
        }
}

class ToDateTimeValueExtractor : BaseToDateTimeValueExtractor() {

    override fun getTemporalQuery(): TemporalQuery<*> {
        return TemporalQuery { LocalDateTime.ofInstant(Instant.from(it), UTC) }
    }

    override fun getExtractFunctionName(): String = "toDateTime"
}

class ToDateValueExtractor : BaseToDateTimeValueExtractor() {

    override fun getTemporalQuery(): TemporalQuery<*> {
        return TemporalQuery { LocalDate.from(it) }
    }

    override fun getExtractFunctionName(): String = "toDate"
}
