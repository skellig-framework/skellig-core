package org.skellig.teststep.processing.value.function

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

/**
 * Executes the 'toJson' function to convert the provided value or argument to JSON string representation.
 * If the 'value' is not null, then the function converts it to JSON [String].
 * If the 'value' is null and 1 argument is provided, then it converts it to JSON [String], only if the argument is not [String]
 * (ex. [Any] object, [Map], [List], etc.) - otherwise return the argument as is.
 *
 * Supported args:
 * - toJson() - convert the 'value' to JSON [String]
 * - toJson(`<value to convert>`) - convert the `<value to convert>` to JSON [String] if it's not a [String].
 */
class ToJsonFunctionExecutor : FunctionValueExecutor {

    private val jsonSerializer = ObjectMapper()

    init {
        jsonSerializer.registerModule(JavaTimeModule())
        jsonSerializer.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        return if (value != null) {
            jsonSerializer.writeValueAsString(value)
        } else if (args.size == 1) {
            val argValue = args[0]
            if (argValue != null && argValue !is String) jsonSerializer.writeValueAsString(argValue)
            else argValue
        } else {
            throw FunctionExecutionException("Function `${getFunctionName()}` can only accept 1 argument. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "toJson"
}