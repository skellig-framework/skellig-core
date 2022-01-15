package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.validation.comparator.DateTimeValueComparator
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery
import java.util.regex.Pattern

abstract class BaseToDateTimeValueExtractor : TestStepValueExtractor {

    companion object {
        private val SPLIT_PATTERN = Pattern.compile(",")
         val UTC = ZoneId.of("UTC")
    }

    override fun extract(value: Any?, extractionParameter: String?): Any? =
        when (value) {
            is String -> {
                var pattern: String? = null
                var timezone: String? = null
                val split = SPLIT_PATTERN.split(extractionParameter ?: "")
                if (split.isNotEmpty()) {
                    pattern = split[0].trim()
                    if (split.size > 1) {
                        timezone = split[1].trim()
                    }
                }
                pattern?.let {
                    val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
                        .withZone(timezone?.let { ZoneId.of(timezone) } ?: UTC)
                    parseDate(value, dateTimeFormatter, getTemporalQuery())
                } ?: throw ValueExtractionException("Date/Time pattern is mandatory for '${getExtractFunctionName()}' function")
            }
            else -> value
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
