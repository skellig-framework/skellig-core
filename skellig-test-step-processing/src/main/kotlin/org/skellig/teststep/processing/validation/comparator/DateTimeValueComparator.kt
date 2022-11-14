package org.skellig.teststep.processing.validation.comparator

import org.skellig.teststep.processing.exception.ValidationException
import java.sql.Timestamp
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Pattern

class DateTimeValueComparator : ValueComparator {

    companion object {
        private val UTC = ZoneId.of("UTC")
        private const val NOW = "now"
        private const val YESTERDAY = "yesterday"
        private const val TOMORROW = "tomorrow"
        private const val MILLISECOND = "millisecond"
        private const val SECOND = "second"
        private const val MINUTE = "minute"
        private const val HOUR = "hour"
        private const val DAY = "day"
        private const val MONTH = "month"
        private const val YEAR = "year"
        private const val AGO = "ago"
        private const val AFTER = "after"
        private val TIME_DIFF_PATTERN =
            Pattern.compile("\\s*(\\d+)\\s+(${MILLISECOND}s?|${SECOND}s?|${MINUTE}s?|${HOUR}s?|${DAY}s?|${MONTH}s?|${YEAR}s?)\\s+($AFTER|$AGO)\\s*")

        private val CHRONO_UNIT = mapOf(
            Pair(MILLISECOND, ChronoUnit.MILLIS),
            Pair("${MILLISECOND}s", ChronoUnit.MILLIS),
            Pair(SECOND, ChronoUnit.SECONDS),
            Pair("${SECOND}s", ChronoUnit.SECONDS),
            Pair(MINUTE, ChronoUnit.MINUTES),
            Pair("${MINUTE}s", ChronoUnit.MINUTES),
            Pair(HOUR, ChronoUnit.HOURS),
            Pair("${HOUR}s", ChronoUnit.HOURS),
            Pair(DAY, ChronoUnit.DAYS),
            Pair("${DAY}s", ChronoUnit.DAYS),
            Pair(MONTH, ChronoUnit.MONTHS),
            Pair("${MONTH}s", ChronoUnit.MONTHS),
            Pair(YEAR, ChronoUnit.YEARS),
            Pair("${YEAR}s", ChronoUnit.YEARS),
        )
        private val TIME_OPERATION = mapOf(
            Pair(AFTER) { time: LocalDateTime, amount: Long, unit: ChronoUnit -> time.plus(amount, unit) },
            Pair(AGO) { time: LocalDateTime, amount: Long, unit: ChronoUnit -> time.minus(amount, unit) },
        )
    }

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        val format = if (args.size >= 3) args[2]?.toString() else null
        val timezone = if (args.size == 4) args[3]?.toString() else null
        val min = getDateFromString(args[0].toString(), format, timezone)
        val max = getDateFromString(args[1].toString(), format, timezone)
        return when (actualValue) {
            is LocalDate -> isDateBetween(actualValue, min.toLocalDate(), max.toLocalDate())
            is LocalDateTime -> isBetween(actualValue, min, max)
            is Date -> isBetween(LocalDateTime.ofInstant(Instant.ofEpochMilli(actualValue.time), UTC), min, max)
            is java.sql.Date -> isBetween(LocalDateTime.ofInstant(Instant.ofEpochMilli(actualValue.time), UTC), min, max)
            is Instant -> isBetween(LocalDateTime.ofInstant(actualValue, UTC), min, max)
            is ZonedDateTime -> isBetween(LocalDateTime.ofInstant(actualValue.toInstant(), UTC), min, max)
            is Timestamp -> isBetween(LocalDateTime.ofInstant(actualValue.toInstant(), UTC), min, max)
            else -> {
                if (actualValue is String && format != null) {
                    isBetween(convertToLocalDateTime(actualValue, format, timezone), min, max)
                } else {
                    throw ValidationException(
                        "Cannot compare value '$actualValue' as it is not Date or DateTime type.\n" +
                                "Did you forget to convert the response value toDate() or toDateTime()? " +
                                "Or use date/time format as a 3rd parameter if the response value is a String type"
                    )
                }
            }
        }
    }

    private fun toDateTime(value: String?, format: String?, timezone: String?): LocalDateTime {
        val dateTimeFormatter = createDateTimeFormatter(timezone, format)
        return if (timezone == null) LocalDateTime.parse(value, dateTimeFormatter)
        // convert to ZonedDateTime and then back to LocalDateTime with UTC to preserve time difference from timezone
        else LocalDateTime.ofInstant(ZonedDateTime.parse(value, dateTimeFormatter).toInstant(), UTC)
    }

    private fun toDate(value: String?, format: String?, timezone: String?): LocalDate {
        val dateTimeFormatter = createDateTimeFormatter(timezone, format)
        return if (timezone == null) LocalDate.parse(value, dateTimeFormatter)
        // convert to ZonedDateTime and then back to LocalDateTime with UTC to preserve time difference from timezone
        else LocalDate.ofInstant(ZonedDateTime.parse(value, dateTimeFormatter).toInstant(), UTC)
    }

    private fun createDateTimeFormatter(timezone: String?, format: String?): DateTimeFormatter? {
        return if (timezone != null) DateTimeFormatter.ofPattern(format).withZone(ZoneOffset.of(timezone))
        else DateTimeFormatter.ofPattern(format)
    }

    private fun isBetween(value: LocalDateTime, min: LocalDateTime, max: LocalDateTime) =
        (value.isAfter(min) || value == min) && (value.isBefore(max) || value == max)

    private fun isDateBetween(value: LocalDate, min: LocalDate, max: LocalDate) =
        (value.isAfter(min) || value == min) && (value.isBefore(max) || value == max)

    private fun getDateFromString(date: String, format: String?, timezone: String?): LocalDateTime =
        when (date.trim()) {
            NOW -> LocalDateTime.now()
            YESTERDAY -> LocalDateTime.now().minusDays(1)
            TOMORROW -> LocalDateTime.now().plusDays(1)
            else -> {
                val matcher = TIME_DIFF_PATTERN.matcher(date.trim())
                var dateTime: LocalDateTime? = null
                if (matcher.find()) {
                    val amount = matcher.group(1).toLong()
                    val unit = matcher.group(2)
                    val operation = matcher.group(3)

                    dateTime = TIME_OPERATION[operation]?.invoke(
                        LocalDateTime.now(),
                        amount,
                        CHRONO_UNIT[unit] ?: error("Invalid Chrono unit $unit. Supported are: ${CHRONO_UNIT.keys}")
                    )

                } else if (format != null) {
                    dateTime = convertToLocalDateTime(date.trim(), format, timezone)
                }

                if (dateTime == null) {
                    throw ValidationException(
                        "Cannot process date '${date.trim()}' in `between` comparator as it is not parseable to date or time.\n" +
                                "Supported formats are: $NOW, $YESTERDAY, $TOMORROW or should match $TIME_DIFF_PATTERN."
                    )
                }
                dateTime
            }
        }

    private fun convertToLocalDateTime(value: String, format: String?, timezone: String?) =
        try {
            toDateTime(value, format, timezone)
        } catch (ex: DateTimeParseException) {
            // in case if 'date' var is date without time, try to convert to LocalDate
            val date = toDate(value, format, timezone)
            LocalDateTime.of(date.year, date.month, date.dayOfMonth, 0, 0, 0, 0)
        }

    override fun getName(): String = "between"
}