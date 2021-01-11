package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.StringUtils
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.regex.Pattern

class DateTimeValueConverter : TestStepValueConverter {

    companion object {
        private val NOW_REGEX = Pattern.compile("now\\(([\\w]*)\\)")
    }

    override fun convert(value: String?): Any? {
        val matcher = NOW_REGEX.matcher(value ?: "")
        if (matcher.find()) {
            val timezone = matcher.group(1)
            return getLocalDateTime(timezone)
        }
        return value
    }

    private fun getLocalDateTime(timezone: String): Any? {
        return if (StringUtils.isEmpty(timezone)) {
            LocalDateTime.now()
        } else {
            try {
                LocalDateTime.now(ZoneId.of(timezone))
            } catch (ex: DateTimeException) {
                LocalDateTime.now()
            }
        }
    }

}