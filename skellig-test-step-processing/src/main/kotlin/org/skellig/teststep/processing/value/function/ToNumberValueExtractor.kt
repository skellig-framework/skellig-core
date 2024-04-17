package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.math.BigDecimal


/**
 * An abstract class representing a function executor which converts [Number] or [String] to another [Number],
 * or [String] ot [Boolean].
 */
abstract class ToNumberFunctionExecutor : FunctionValueExecutor {

    protected fun getParseException(value: Any?): FunctionExecutionException =
        FunctionExecutionException("Failed to extract numeric value from type ${value?.javaClass?.simpleName} for value: $value")
}

class ToIntFunctionExecutor : ToNumberFunctionExecutor() {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toInt() ?: (value as? String ?: throw getParseException(value)).toInt()
    }

    override fun getFunctionName(): String {
        return "toInt"
    }
}

class ToByteFunctionExecutor : ToNumberFunctionExecutor() {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toByte() ?: (value as? String ?: throw getParseException(value)).toByte()
    }

    override fun getFunctionName(): String {
        return "toByte"
    }
}

class ToShortFunctionExecutor : ToNumberFunctionExecutor() {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toShort() ?: (value as? String ?: throw getParseException(value)).toShort()
    }

    override fun getFunctionName(): String {
        return "toShort"
    }
}

class ToLongFunctionExecutor : ToNumberFunctionExecutor() {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toLong() ?: (value as? String ?: throw getParseException(value)).toLong()
    }

    override fun getFunctionName(): String {
        return "toLong"
    }
}

class ToFloatFunctionExecutor : ToNumberFunctionExecutor() {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toFloat() ?: (value as? String ?: throw getParseException(value)).toFloat()
    }

    override fun getFunctionName(): String {
        return "toFloat"
    }
}

class ToDoubleFunctionExecutor : ToNumberFunctionExecutor() {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Number)?.toDouble() ?: (value as? String ?: throw getParseException(value)).toDouble()
    }

    override fun getFunctionName(): String {
        return "toDouble"
    }
}

class ToBigDecimalFunctionExecutor : ToNumberFunctionExecutor() {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return when (value) {
            is BigDecimal -> value
            is Number -> BigDecimal(value.toString())
            else -> (value as? String ?: throw getParseException(value)).toBigDecimal()
        }
    }

    override fun getFunctionName(): String {
        return "toBigDecimal"
    }
}

class ToBooleanFunctionExecutor : ToNumberFunctionExecutor() {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return (value as? Boolean)?: (value as? String ?: throw getParseException(value)).toBoolean()
    }

    override fun getFunctionName(): String {
        return "toBoolean"
    }
}