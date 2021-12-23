package org.skellig.performance.runner.service.controller

import org.skellig.teststep.processor.performance.model.factory.PerformanceTestStepFactory
import org.skellig.teststep.runner.context.SkelligTestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

@Controller
class SkelligPerformancePageController {

    companion object {
        private val PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_]+)(\\s*:\\s*(.+))?}")
        private const val RPS = "test.step.keyword.rps"
        private const val TIME_TO_RUN = "test.step.keyword.timeToRun"
        private const val BEFORE = "test.step.keyword.before"
        private const val AFTER = "test.step.keyword.after"
        private const val RUN = "test.step.keyword.run"
        private const val NAME = "test.step.keyword.name"

        val TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    @Autowired
    private var skelligTestContext: SkelligTestContext? = null

    @Autowired
    private var configPath: String? = null

    @Autowired
    @Qualifier("testSteps")
    private var testSteps: List<String>? = null

    @GetMapping("/")
    fun home(model: Model): String {
        skelligTestContext?.initialize(
            javaClass.classLoader,
            testSteps ?: error("No paths to test steps were provided"),
            configPath
        )
        val testStepRegistry = skelligTestContext!!.getTestStepRegistry()
        val properties = skelligTestContext!!.testStepKeywordsProperties
        var idCounter = 0
        model["testItems"] = testStepRegistry.getTestSteps()
            .filter { it.containsKey(getRpsKeyword(properties)) }
            .map {
                val name = it[getKeywordName(NAME, "name", properties)].toString()
                val rps = getRps(it, properties)
                val timeToRun = getTimeToRun(it, properties)
                val parameters = findAllParameters(it, properties)
                PerformanceTestDetails(
                    (idCounter++).toString(), name, timeToRun?.first, timeToRun?.second,
                    rps?.first, rps?.second, parameters
                )
            }

        return "index"
    }

    private fun getTimeToRun(rawTestStep: Map<String, Any?>, properties: Properties?): Pair<String?, String?>? {
        val timeToRun = rawTestStep[getTimeToRunKeyword(properties)]?.toString()
        return timeToRun?.let {
            tryExtractParameterWithDefaultValue(it)
                ?: Pair(null, LocalTime.parse(it, TIME_PATTERN).format(TIME_PATTERN))
        }
    }

    private fun getRps(rawTestStep: Map<String, Any?>, properties: Properties?): Pair<String?, String?>? {
        val rps = rawTestStep[getRpsKeyword(properties)]?.toString()
        return rps?.let {
            tryExtractParameterWithDefaultValue(it) ?: Pair(null, it)
        }
    }

    private fun findAllParameters(rawTestStep: Map<String, Any?>, properties: Properties?): Collection<Parameter> {
        val rawTestSteps = mutableListOf<String>()
        val testStepRegistry = skelligTestContext!!.getTestStepRegistry()
        (rawTestStep[getKeywordName(BEFORE, "before", properties)] as List<*>?)?.forEach {
            rawTestSteps.add(it.toString())
        }
        (rawTestStep[getKeywordName(AFTER, "after", properties)] as List<*>?)?.forEach {
            rawTestSteps.add(it.toString())
        }
        (rawTestStep[getKeywordName(RUN, "run", properties)] as List<*>?)?.forEach {
            rawTestSteps.add(it.toString())
        }
        return rawTestSteps.flatMap {
            findAllParameters(testStepRegistry.getByName(it))
        }.union(findAllParameters(rawTestStep.filter {
            it.key != getTimeToRunKeyword(properties) && it.key != getRpsKeyword(properties)
        }))
    }

    private fun findAllParameters(rawTestStep: Map<String, Any?>?): Collection<Parameter> {
        val variables = rawTestStep?.get("variables") as Map<String, Any?>?
        return rawTestStep?.flatMap {
            val parameterNames = mutableSetOf<Parameter>()
            val matcher = PARAMETER_REGEX.matcher(it.key)
            while (matcher.find()) {
                val paramName = matcher.group(1)
                if (variables?.containsKey(paramName) == false)
                    parameterNames.add(Parameter(paramName))
            }
            val valueMatcher = PARAMETER_REGEX.matcher(it.value?.toString() ?: "")
            while (valueMatcher.find()) {
                val paramName = valueMatcher.group(1)
                if (variables?.containsKey(paramName) == false)
                    parameterNames.add(Parameter(paramName))
            }
            parameterNames
        }?.toSet() ?: emptyList()
    }

    private fun tryExtractParameterWithDefaultValue(value: String): Pair<String?, String?>? {
        val matcher = PARAMETER_REGEX.matcher(value)
        return if (matcher.find()) {
            if (matcher.groupCount() > 2) Pair(matcher.group(1), matcher.group(3))
            else Pair(matcher.group(1), null)
        } else null
    }

    private fun getTimeToRunKeyword(properties: Properties?) =
        getKeywordName(TIME_TO_RUN, "timeToRun", properties)

    private fun getRpsKeyword(properties: Properties?) = getKeywordName(RPS, "rps", properties)

    private fun getKeywordName(keywordName: String?, defaultValue: String, properties: Properties?): String {
        return if (properties == null) defaultValue else properties.getProperty(keywordName, defaultValue)
    }

    class PerformanceTestDetails(
        val id: String?,
        val name: String,
        val timeToRunParam: String?,
        val timeToRun: String?,
        val rpsParam: String?,
        val rps: String?,
        val parameters: Collection<Parameter>?
    ) {
        constructor() : this("0", "", null, null, null, null, null)

        fun isRpsSet(): Boolean = rpsParam == null && rps != null

        fun isTimeToRunSet(): Boolean = timeToRunParam == null && timeToRun != null

        fun toMapParameters(): Map<String, String?> {
            val parameters =
                HashMap(parameters?.associate { it.paramName to it.paramValue.toString() } ?: emptyMap())
            timeToRunParam?.let {
                parameters[it] = timeToRun
            }
            rpsParam?.let {
                parameters[it] = rps
            }
            return parameters
        }

        override fun toString(): String {
            return name
        }
    }

    class Parameter(
        val paramName: String,
        var paramValue: Any?
    ) {
        constructor() : this("")

        constructor(paramName: String) : this(paramName, null)

        override fun equals(other: Any?): Boolean {
            return (other as? Parameter)?.paramName == paramName
        }

        override fun hashCode(): Int {
            return paramName.hashCode()
        }
    }
}