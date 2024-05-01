package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val DEFAULT_TIMEZONE = "+0"

/**
 * Executes the "toInstant" function, which converts a [LocalDateTime] value to [Instant] with supplied timezone.
 * If no timezone is supplied then uses UTC.
 *
 * Supported args:
 * - toInstant() - converts [LocalDateTime] to [Instant] with UTC timezone (ex. same as toInstant("+0"))
 * - toInstant(`zone offset`) - for example: toInstant("+2"), converts [LocalDateTime] to [Instant] with timezone offset of +2 hours
 * - toInstant(`zone offset`, `<truncate to>`) - for example: toInstant("+0", Millis), converts [LocalDateTime] to [Instant]
 * with timezone offset of +0 hours (UTC) and truncates to one of the value from [ChronoUnit.name]
 */
class ToInstantFunctionExecutor : FunctionValueExecutor {

private val chronoUnits = ChronoUnit.entries.associateBy { it.toString() }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        return when (value) {
            is LocalDateTime -> {
                val zoneOffset = getTimezone(args) ?: DEFAULT_TIMEZONE
                val instant = value.toInstant(ZoneOffset.of(zoneOffset))
                if (args.size == 2)
                    instant.truncatedTo(chronoUnits[(args[1]?.toString()?.trim() ?: "")]
                        ?:error("Failed to truncate Instant with argument '${args[1]}' when calling function `${getFunctionName()}`"))
                else instant
            }

            else -> throw FunctionExecutionException("Function `${getFunctionName()}` can only be called on LocalDateTime value. Found ${value?.javaClass?.simpleName}")
        }
    }

    private fun getTimezone(args: Array<Any?>) =
        if (args.isNotEmpty()) args[0]?.toString()?.trim() else null

    override fun getFunctionName(): String = "toInstant"
}