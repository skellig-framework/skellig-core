package org.skellig.teststep.processing.value.function

import com.fasterxml.jackson.databind.ObjectMapper
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class JsonToMapTestStepFunctionExecutor : JsonToTestStepFunctionExecutor() {

    override fun getToClassConversion(): Class<*> = Map::class.java

    override fun getDefaultValueForNull(): Any = emptyMap<Any, Any>()

    override fun getFunctionName(): String = "jsonToMap"
}

class JsonToListTestStepFunctionExecutor : JsonToTestStepFunctionExecutor() {

    override fun getToClassConversion(): Class<*> = List::class.java

    override fun getDefaultValueForNull(): Any = emptyList<Any>()

    override fun getFunctionName(): String = "jsonToList"

}

abstract class JsonToTestStepFunctionExecutor : FunctionValueExecutor {

    private val objectMapper = ObjectMapper()

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        val valueAsString = value?.toString() ?: ""
        return if (value == null || valueAsString.isEmpty()) {
            getDefaultValueForNull()
        } else {
            try {
                objectMapper.readValue(value.toString(), getToClassConversion())
            } catch (ex: Exception) {
                throw FunctionExecutionException(
                    "Failed to convert JSON to ${getToClassConversion().simpleName}: '$value'",
                    ex
                )
            }
        }
    }

    abstract fun getToClassConversion(): Class<*>

    abstract fun getDefaultValueForNull(): Any
}