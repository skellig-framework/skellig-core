package org.skellig.teststep.processing.value.function

import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Executes 'now' function to get the current date and time [LocalDateTime] with or without timezone.
 * Supported arguments:
 * - now() - returns [LocalDateTime.now]
 * - now(`<timezone>`) - for example: now(UTC) returns [LocalDateTime.now] with UTC timezone
 * - now(`<timezone>`, `<format>`) - for example: now(UTC, yyyy-MM-dd'T'HH:mm) returns formatted [LocalDateTime.now] as String for the timezone
 *
 * If 'value' is provided, then throws [FunctionExecutionException] as this function is independent.
 */
class CurrentDateTimeFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        if (value != null) throw FunctionExecutionException("Function `${getFunctionName()}` cannot be called from another value")

        val timezone = if (args.isNotEmpty()) args[0]?.toString() ?: "" else ""
        val format = if (args.size == 2) args[1] else null
        return if (format == null) getLocalDateTime(timezone)
        else getLocalDateTime(format.toString(), timezone)
    }

    private fun getLocalDateTime(timezone: String): LocalDateTime {
        return if (StringUtils.isEmpty(timezone)) {
            LocalDateTime.now()
        } else {
            try {
                LocalDateTime.now(ZoneId.of(timezone))
            } catch (ex: DateTimeException) {
                throw FunctionExecutionException("Cannot get current date for the timezone '$timezone'")
            }
        }
    }

    private fun getLocalDateTime(format: String, timezone: String): String {
        val now = getLocalDateTime(timezone)
        return try {
            now.format(DateTimeFormatter.ofPattern(format))
        } catch (ex: Exception) {
            throw FunctionExecutionException("Cannot format current date with the format '$format'")
        }
    }

    override fun getFunctionName(): String = "now"

}