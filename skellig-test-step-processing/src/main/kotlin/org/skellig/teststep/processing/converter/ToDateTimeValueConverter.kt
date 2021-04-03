package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.exception.TestValueConversionException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQuery
import java.util.regex.Pattern

class ToDateTimeValueConverter : TestStepValueConverter {

    companion object {
        private val TO_DATE_PATTERN = Pattern.compile("toDate\\(([\\w\\s-]+)\\)")
        private val TO_DATE_TIME_PATTERN = Pattern.compile("toDateTime\\(([\\w\\s-:]+)\\)")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    }

    override fun convert(value: Any?): Any? =
            value?.let {
                var matcher = TO_DATE_PATTERN.matcher(value.toString())
                if (matcher.find()) {
                    val date = matcher.group(1)
                    parseDate(date, DATE_FORMATTER) { LocalDate.from(it) }
                } else {
                    matcher = TO_DATE_TIME_PATTERN.matcher(value.toString())
                    if (matcher.find()) {
                        val dateTime = matcher.group(1)
                        parseDate(dateTime, DATE_TIME_FORMATTER) { LocalDateTime.from(it) }
                    } else value
                }
            }

    private fun parseDate(value: String?, formatter: DateTimeFormatter, query: TemporalQuery<*>) =
            try {
                formatter.parse(value, query)
            } catch (ex: Exception) {
                throw TestValueConversionException("Failed to convert date $value by pattern $formatter");
            }
}