package org.skellig.teststep.processing.value.function

import io.restassured.path.json.JsonPath
import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class JsonPathFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        if (args.isNotEmpty()) {
            return value?.let {
                try {
                    val json = JsonPath.from(it as String)
                    json.getString(args[0]?.toString())
                } catch (ex: Exception) {
                    if (args.size == 1 || (args.size == 2 && args[1]?.toString()?.trim() != "true")) {
                        throw FunctionExecutionException("Failed to extract jsonPath '${args[0]}' from value '$value'. Reason ${ex.message}")
                    } else null
                }
            }
        } else {
            throw FunctionExecutionException("Function `jsonPath` can only accept 1 or 2 arguments. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String {
        return "jsonPath"
    }

}