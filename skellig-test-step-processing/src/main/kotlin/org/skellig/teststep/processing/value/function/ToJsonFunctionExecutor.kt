package org.skellig.teststep.processing.value.function

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException

class ToJsonFunctionExecutor : FunctionValueExecutor {

    private val jsonSerializer = ObjectMapper()

    init {
        jsonSerializer.registerModule(JavaTimeModule())
        jsonSerializer.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    override fun execute(name: String, args: Array<Any?>): Any? {
        return if (args.size == 1) {
            jsonSerializer.writeValueAsString(args[0])
        } else {
            throw FunctionValueExecutionException("Function `${getFunctionName()}` can only accept 1 argument. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "toJson"
}