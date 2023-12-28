package org.skellig.teststep.processing.value.config

import com.typesafe.config.Config
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.function.FunctionValueExecutor

interface FunctionsConfig {
    fun configFunctionExecutors(details: FunctionsConfigDetails): List<FunctionValueExecutor>
}

data class FunctionsConfigDetails(
    val state: TestScenarioState,
    val config: Config,
)
