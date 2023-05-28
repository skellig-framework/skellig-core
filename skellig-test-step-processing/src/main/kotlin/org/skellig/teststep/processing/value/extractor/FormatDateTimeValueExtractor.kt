package org.skellig.teststep.processing.value.extractor

import org.skellig.teststep.processing.value.extractor.exception.ValueExtractionException
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

abstract class BaseFormatDateTimeValueExtractor : ValueExtractor {

    companion object {
        internal val UTC = ZoneId.of("UTC")
    }

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return if (args.isNotEmpty()) {
            when (value) {
                is TemporalAccessor -> {
                    var timezone: String? = null
                    val pattern = args[0]?.toString()?.trim()
                    if (args.size == 2) {
                        timezone = args[1]?.toString()?.trim()
                    }
                    pattern?.let {
                        val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
                            .withZone(timezone?.let { ZoneId.of(timezone) } ?: UTC)
                        try {
                            dateTimeFormatter.format(value)
                        } catch (ex: Exception) {
                            throw ValueExtractionException(
                                "Failed to format date $value with pattern $dateTimeFormatter",
                                ex
                            );
                        }
                    }
                        ?: throw ValueExtractionException("Date/Time pattern is mandatory for '${getExtractFunctionName()}' function")
                }

                else -> throwInvalidTypeException(value?.javaClass)
            }
        } else throw FunctionValueExecutionException("Function `${getExtractFunctionName()}` can only accept 1 or 2 String arguments. Found ${args.size}")
    }

    private fun throwInvalidTypeException(type: Class<*>?) {
        throw FunctionValueExecutionException("Invalid type for '${getExtractFunctionName()}' - ${type?.simpleName}. " +
                "Must be accessible from TemporalAccessor (ex. LocalDateTime)")
    }
}

class FormatDateTimeValueExtractor : BaseFormatDateTimeValueExtractor() {

    override fun getExtractFunctionName(): String = "formatDateTime"
}

class FormatDateValueExtractor : BaseFormatDateTimeValueExtractor() {

    override fun getExtractFunctionName(): String = "formatDate"
}
