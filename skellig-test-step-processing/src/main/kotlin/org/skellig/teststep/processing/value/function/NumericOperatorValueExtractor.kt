package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.math.BigDecimal

abstract class NumericOperatorTestStepValueExtractor : FunctionValueExecutor {

    protected fun toBigDecimal(extractionParameter: String?) =
        BigDecimal(extractionParameter ?: throw getParseException(extractionParameter))

    protected fun toLong(extractionParameter: String?) =
        extractionParameter?.toLongOrNull() ?: throw getParseException(extractionParameter)

    protected fun toDouble(extractionParameter: String?) =
        extractionParameter?.toDoubleOrNull() ?: throw getParseException(extractionParameter)

    protected fun toFloat(extractionParameter: String?) =
        extractionParameter?.toFloatOrNull() ?: throw getParseException(extractionParameter)

    protected fun toShort(extractionParameter: String?) =
        extractionParameter?.toShortOrNull() ?: throw getParseException(extractionParameter)

    protected fun toByte(extractionParameter: String?) =
        extractionParameter?.toByteOrNull() ?: throw getParseException(extractionParameter)

    protected fun toInt(extractionParameter: String?) =
        extractionParameter?.toIntOrNull() ?: throw getParseException(extractionParameter)

    protected fun getParseException(extractionParameter: String?): FunctionExecutionException =
        FunctionExecutionException("Failed to parse $extractionParameter to numeric type")
}