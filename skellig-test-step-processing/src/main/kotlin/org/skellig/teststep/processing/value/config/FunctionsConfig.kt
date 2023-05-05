package org.skellig.teststep.processing.value.config

import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.extractor.ValueExtractor
import org.skellig.teststep.processing.value.function.FunctionValueExecutor

interface FunctionsConfig {
    fun configValueExtractors(details: FunctionsConfigDetails): List<ValueExtractor>

    fun configFunctionExecutors(details: FunctionsConfigDetails): List<FunctionValueExecutor>
}

data class FunctionsConfigDetails(
    val state: TestScenarioState,
//    val config: Config,
)
