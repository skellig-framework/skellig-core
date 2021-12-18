package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import java.math.BigDecimal

abstract class ToNumberTestStepValueExtractor : TestStepValueExtractor {

    protected fun getParseException(value: Any?): ValueExtractionException =
        ValueExtractionException("Failed to extract numeric value from type ${value?.javaClass?.simpleName}")
}

class ToIntTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value as? Int ?: (value as? String ?: throw getParseException(value)).toInt()
    }

    override fun getExtractFunctionName(): String {
        return "toInt"
    }
}

class ToByteTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value as? Byte ?: (value as? String ?: throw getParseException(value)).toByte()
    }

    override fun getExtractFunctionName(): String {
        return "toByte"
    }
}

class ToShortTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value as? Short ?: (value as? String ?: throw getParseException(value)).toShort()
    }

    override fun getExtractFunctionName(): String {
        return "toShort"
    }
}

class ToLongTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value as? Long ?: (value as? String ?: throw getParseException(value)).toLong()
    }

    override fun getExtractFunctionName(): String {
        return "toLong"
    }
}

class ToFloatTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value as? Float ?: (value as? String ?: throw getParseException(value)).toFloat()
    }

    override fun getExtractFunctionName(): String {
        return "toFloat"
    }
}

class ToDoubleTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value as? Double ?: (value as? String ?: throw getParseException(value)).toDouble()
    }

    override fun getExtractFunctionName(): String {
        return "toDouble"
    }
}

class ToBigDecimalTestStepValueExtractor : ToNumberTestStepValueExtractor() {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value as? BigDecimal ?: (value as? String ?: throw getParseException(value)).toBigDecimal()
    }

    override fun getExtractFunctionName(): String {
        return "toBigDecimal"
    }
}