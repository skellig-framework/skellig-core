package org.skellig.teststep.processing.value.extractor

import org.skellig.teststep.processing.value.extractor.exception.ValueExtractionException
import java.math.BigDecimal
import kotlin.math.pow

abstract class NumericOperatorTestStepValueExtractor : ValueExtractor {

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

    protected fun getParseException(extractionParameter: String?): ValueExtractionException =
        ValueExtractionException("Failed to parse $extractionParameter to numeric type")
}

class PlusOperatorTestStepValueExtractor : NumericOperatorTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return value?.let {
            val extractionParameter = args[0]?.toString()
            when (it) {
                is String -> (it.toIntOrNull() ?: throw getParseException(it)).plus(toInt(extractionParameter))
                is Byte -> it.plus(toByte(extractionParameter)).toByte()
                is Short -> it.plus(toShort(extractionParameter)).toShort()
                is Int -> it.plus(toInt(extractionParameter))
                is Float -> it.plus(toFloat(extractionParameter))
                is Double -> it.plus(toDouble(extractionParameter))
                is Long -> it.plus(toLong(extractionParameter))
                is BigDecimal -> it.plus(toBigDecimal(extractionParameter))
                else -> throw ValueExtractionException("Cannot apply 'plus' operator to type '${value.javaClass}'")
            }
        }
    }

    override fun getExtractFunctionName(): String {
        return "plus"
    }
}

class MinusOperatorTestStepValueExtractor : NumericOperatorTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return value?.let {
            val extractionParameter = args[0]?.toString()
            when (it) {
                is String -> (it.toIntOrNull() ?: throw getParseException(it)).minus(toInt(extractionParameter))
                is Byte -> it.minus(toByte(extractionParameter)).toByte()
                is Short -> it.minus(toShort(extractionParameter)).toShort()
                is Int -> it.minus(toInt(extractionParameter))
                is Float -> it.minus(toFloat(extractionParameter))
                is Double -> it.minus(toDouble(extractionParameter))
                is Long -> it.minus(toLong(extractionParameter))
                is BigDecimal -> it.minus(toBigDecimal(extractionParameter))
                else -> throw ValueExtractionException("Cannot apply 'plus' operator to type '${value.javaClass}'")
            }
        }
    }

    override fun getExtractFunctionName(): String {
        return "minus"
    }
}

class TimesOperatorTestStepValueExtractor : NumericOperatorTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return value?.let {
            val extractionParameter = args[0]?.toString()
            when (it) {
                is String -> (it.toIntOrNull() ?: throw getParseException(it)).times(toInt(extractionParameter))
                is Byte -> it.times(toByte(extractionParameter)).toByte()
                is Short -> it.times(toShort(extractionParameter)).toShort()
                is Int -> it.times(toInt(extractionParameter))
                is Float -> it.times(toFloat(extractionParameter))
                is Double -> it.times(toDouble(extractionParameter))
                is Long -> it.times(toLong(extractionParameter))
                is BigDecimal -> it.times(toBigDecimal(extractionParameter))
                else -> throw ValueExtractionException("Cannot apply 'plus' operator to type '${value.javaClass}'")
            }
        }
    }

    override fun getExtractFunctionName(): String {
        return "times"
    }
}

class DivOperatorTestStepValueExtractor : NumericOperatorTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return value?.let {
            val extractionParameter = args[0]?.toString()
            when (it) {
                is String -> (it.toIntOrNull() ?: throw getParseException(it)).div(toInt(extractionParameter))
                is Byte -> it.div(toByte(extractionParameter)).toByte()
                is Short -> it.div(toShort(extractionParameter)).toShort()
                is Int -> it.div(toInt(extractionParameter))
                is Float -> it.div(toFloat(extractionParameter))
                is Double -> it.div(toDouble(extractionParameter))
                is Long -> it.div(toLong(extractionParameter))
                is BigDecimal -> it.div(toBigDecimal(extractionParameter))
                else -> throw ValueExtractionException("Cannot apply 'plus' operator to type '${value.javaClass}'")
            }
        }
    }

    override fun getExtractFunctionName(): String {
        return "div"
    }
}

class PowOperatorTestStepValueExtractor : NumericOperatorTestStepValueExtractor() {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return value?.let {
            val extractionParameter = args[0]?.toString()
            when (it) {
                is String ->
                    (it.toDoubleOrNull() ?: throw getParseException(it)).pow(toDouble(extractionParameter))
                is Number -> {
                    it.toDouble().pow(toDouble(extractionParameter))
                }
                else -> throw ValueExtractionException("Cannot apply 'plus' operator to type '${value.javaClass}'")
            }
        }
    }

    override fun getExtractFunctionName(): String {
        return "pow"
    }
}