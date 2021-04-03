package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class CurrentDateTimeValueConverter : TestStepValueConverter {

    companion object {
        private val NOW_PATTERN = Pattern.compile("now\\(([\\w]*)\\)(.format\\(([\\w-'/:.]*)\\))?")
    }

    override fun convert(value: Any?): Any? =
            when (value) {
                is String -> {
                    val matcher = NOW_PATTERN.matcher(value.toString())
                    if (matcher.find()) {
                        val timezone = matcher.group(1)
                        val format = matcher.group(3)
                        if (format == null) getLocalDateTime(timezone)
                        else value.toString().replace(matcher.group(0), getLocalDateTime(format, timezone))
                    } else value
                }
                else -> value
            }

    private fun getLocalDateTime(timezone: String): LocalDateTime {
        return if (StringUtils.isEmpty(timezone)) {
            LocalDateTime.now()
        } else {
            try {
                LocalDateTime.now(ZoneId.of(timezone))
            } catch (ex: DateTimeException) {
                throw TestDataConversionException("Cannot get current date for the timezone '$timezone'")
            }
        }
    }

    private fun getLocalDateTime(format: String, timezone: String): String {
        val now = getLocalDateTime(timezone)
        return try {
            now.format(DateTimeFormatter.ofPattern(format))
        } catch (ex: Exception) {
            throw TestDataConversionException("Cannot format current date with the format '$format'")
        }
    }

}