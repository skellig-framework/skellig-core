package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor
import java.math.BigDecimal

abstract class ToNumberTestStepValueExtractor : TestStepValueExtractor, ValueExtractor {

    protected fun getParseException(value: Any?): ValueExtractionException =
        ValueExtractionException("Failed to extract numeric value from type ${value?.javaClass?.simpleName} for value: $value")
}

class ToIntTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toInt() ?: (value as? String ?: throw getParseException(value)).toInt()
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return (value as? Number)?.toInt() ?: (value as? String ?: throw getParseException(value)).toInt()
    }

    override fun getExtractFunctionName(): String {
        return "toInt"
    }
}

class ToByteTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toByte() ?: (value as? String ?: throw getParseException(value)).toByte()
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return (value as? Number)?.toByte() ?: (value as? String ?: throw getParseException(value)).toByte()
    }

    override fun getExtractFunctionName(): String {
        return "toByte"
    }
}

class ToShortTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toShort() ?: (value as? String ?: throw getParseException(value)).toShort()
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return (value as? Number)?.toShort() ?: (value as? String ?: throw getParseException(value)).toShort()
    }

    override fun getExtractFunctionName(): String {
        return "toShort"
    }
}

class ToLongTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toLong() ?: (value as? String ?: throw getParseException(value)).toLong()
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return (value as? Number)?.toLong() ?: (value as? String ?: throw getParseException(value)).toLong()
    }

    override fun getExtractFunctionName(): String {
        return "toLong"
    }
}

class ToFloatTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toFloat() ?: (value as? String ?: throw getParseException(value)).toFloat()
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return (value as? Number)?.toFloat() ?: (value as? String ?: throw getParseException(value)).toFloat()
    }

    override fun getExtractFunctionName(): String {
        return "toFloat"
    }
}

class ToDoubleTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toDouble() ?: (value as? String ?: throw getParseException(value)).toDouble()
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return (value as? Number)?.toDouble() ?: (value as? String ?: throw getParseException(value)).toDouble()
    }

    override fun getExtractFunctionName(): String {
        return "toDouble"
    }
}

class ToBigDecimalTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return when (value) {
            is BigDecimal -> value
            is Number -> BigDecimal(value.toString())
            else -> (value as? String ?: throw getParseException(value)).toBigDecimal()
        }
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return when (value) {
            is BigDecimal -> value
            is Number -> BigDecimal(value.toString())
            else -> (value as? String ?: throw getParseException(value)).toBigDecimal()
        }
    }

    override fun getExtractFunctionName(): String {
        return "toBigDecimal"
    }
}

class ToBooleanTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Boolean)?: (value as? String ?: throw getParseException(value)).toBoolean()
    }

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return (value as? Number)?.toDouble() ?: (value as? String ?: throw getParseException(value)).toDouble()
    }

    override fun getExtractFunctionName(): String {
        return "toBoolean"
    }
}