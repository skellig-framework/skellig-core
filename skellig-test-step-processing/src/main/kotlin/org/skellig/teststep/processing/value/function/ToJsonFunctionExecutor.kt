package org.skellig.teststep.processing.value.function

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

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
            if (argValue !is String) jsonSerializer.writeValueAsString(argValue)
            else argValue
        } else {
            throw FunctionExecutionException("Function `${getFunctionName()}` can only accept 1 argument. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "toJson"
}