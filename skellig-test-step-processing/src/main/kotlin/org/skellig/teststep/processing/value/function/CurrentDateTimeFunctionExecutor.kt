package org.skellig.teststep.processing.value.function

import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CurrentDateTimeFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, args: Array<Any?>): Any {
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
                throw FunctionValueExecutionException("Cannot get current date for the timezone '$timezone'")
            }
        }
    }

    private fun getLocalDateTime(format: String, timezone: String): String {
        val now = getLocalDateTime(timezone)
        return try {
            now.format(DateTimeFormatter.ofPattern(format))
        } catch (ex: Exception) {
            throw FunctionValueExecutionException("Cannot format current date with the format '$format'")
        }
    }

    override fun getFunctionName(): String = "now"

}