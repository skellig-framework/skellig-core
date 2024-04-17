package org.skellig.teststep.processing.value.config

import com.typesafe.config.Config
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.function.Function
import org.skellig.teststep.processing.value.function.FunctionValueExecutor

/**
 * The FunctionsConfig interface provides a method for configuring [FunctionValueExecutor]s.
 * All custom functions can be registered by implementing this interface in order to use them in Skellig Test Step files.
 * The framework pick it up automatically when [SkelligTestContext][org.skellig.teststep.runner.context.SkelligTestContext] is initialized.
 *
 * The other way of registering custom functions is marking Kotlin/Java methods with @[Function].
 */
interface FunctionsConfig {
    fun configFunctionExecutors(details: FunctionsConfigDetails): List<FunctionValueExecutor>
}

data class FunctionsConfigDetails(
    val state: TestScenarioState,
    val config: Config,
)
