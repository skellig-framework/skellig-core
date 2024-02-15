package org.skellig.performance.runner.service.controller

import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ListValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
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
        private val PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_]+)(\\s*,\\s*(.+))?}")
        private val RPS = AlphanumericValueExpression("rps")
        private val TIME_TO_RUN = AlphanumericValueExpression("timeToRun")
        private val BEFORE = AlphanumericValueExpression("before")
        private val AFTER = AlphanumericValueExpression("after")
        private val RUN = AlphanumericValueExpression("run")
        private val NAME = AlphanumericValueExpression("name")

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
        val valueExpressionContextFactory = skelligTestContext!!.getValueExpressionContextFactory()
        var idCounter = 0
        model["testItems"] = testStepRegistry.getTestSteps()
            .filter { it.containsKey(RPS) }
            .map {
                val name = it[NAME].toString()
                val parameters = findAllParameters(it)
                val rps = getRps(it, valueExpressionContextFactory)
                val timeToRun = getTimeToRun(it, valueExpressionContextFactory)
                PerformanceTestDetails(
                    (idCounter++).toString(), name, timeToRun?.first, timeToRun?.second,
                    rps?.first, rps?.second, parameters
                )
            }

        return "index"
    }

    private fun getTimeToRun(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        valueExpressionContextFactory: ValueExpressionContextFactory,
    ): Pair<String?, String?>? {
        val timeToRun = rawTestStep[TIME_TO_RUN]?.evaluate(valueExpressionContextFactory.create(emptyMap()))?.toString()
        return timeToRun?.let {
            tryExtractParameterWithDefaultValue(it)
                ?: Pair(null, LocalTime.parse(it, TIME_PATTERN).format(TIME_PATTERN))
        }
    }

    private fun getRps(
        rawTestStep: Map<ValueExpression, ValueExpression?>,
        valueExpressionContextFactory: ValueExpressionContextFactory
    ): Pair<String?, String?>? {
        val rps = rawTestStep[RPS]?.evaluate(valueExpressionContextFactory.create(emptyMap()))?.toString()
        return rps?.let {
            tryExtractParameterWithDefaultValue(it) ?: Pair(null, it)
        }
    }

    private fun findAllParameters(rawTestStep: Map<ValueExpression, ValueExpression?>): Collection<Parameter> {
        val rawTestSteps = mutableListOf<String>()
        val testStepRegistry = skelligTestContext!!.getTestStepRegistry()
        (rawTestStep[BEFORE] as ListValueExpression?)?.value?.forEach {
            rawTestSteps.add(it.toString())
        }
        (rawTestStep[AFTER] as ListValueExpression?)?.value?.forEach {
            rawTestSteps.add(it.toString())
        }
        (rawTestStep[RUN] as ListValueExpression?)?.value?.forEach {
            rawTestSteps.add(it.toString())
        }
        return rawTestSteps.flatMap {
            findAllParametersFrom(testStepRegistry.getByName(it))
        }.union(findAllParametersFrom(rawTestStep.filter {
            it.key != TIME_TO_RUN && it.key != RPS
        }))
    }

    private fun findAllParametersFrom(rawTestStep: Map<ValueExpression, ValueExpression?>?): Collection<Parameter> {
        return rawTestStep?.flatMap {
            val parameterNames = mutableSetOf<Parameter>()
            extractAndAddParameters(it.key.toString(), parameterNames)
            extractAndAddParameters(it.value?.toString() ?: "", parameterNames)
            parameterNames
        }?.toSet() ?: emptyList()
    }

    private fun extractAndAddParameters(value: String, parameterNames: MutableSet<Parameter>) {
        val matcher = PARAMETER_REGEX.matcher(value)
        while (matcher.find()) {
            val paramName = matcher.group(1)
            parameterNames.add(Parameter(paramName))
        }
    }

    private fun tryExtractParameterWithDefaultValue(value: String): Pair<String?, String?>? {
        val matcher = PARAMETER_REGEX.matcher(value)
        return if (matcher.find()) {
            if (matcher.groupCount() > 2) Pair(matcher.group(1), matcher.group(3))
            else Pair(matcher.group(1), null)
        } else null
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