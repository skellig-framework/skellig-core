package org.skellig.teststep.processor.web3

import com.typesafe.config.Config
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestDataProcessingInitException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.web3.model.Web3TestStep
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint208
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

class Web3TestStepProcessor(
    private val web3Nodes: Map<String, Web3j>,
    testScenarioState: TestScenarioState,
    validator: TestStepResultValidator,
    testStepResultConverter: TestStepResultConverter?,
    typeRefs: Map<String, TypeReference<out Type<out Any>>>
) : BaseTestStepProcessor<Web3TestStep>(testScenarioState, validator, testStepResultConverter) {

    private val functionExecutor = Web3MethodExecutor(typeRefs)
    private val eventTaskExecutor = EventTaskExecutor(typeRefs)

    override fun processTestStep(testStep: Web3TestStep): Any? {
        var nodes: Collection<String>? = testStep.nodes
        if (nodes.isNullOrEmpty()) {
            nodes = if (web3Nodes.size > 1) {
                listOf(web3Nodes.keys.first())
            } else {
                web3Nodes.keys
            }
        }

        val tasks = nodes
            .map {
                it to {
                    val node = getNode(it)
                    if (testStep.method != null) {
                        functionExecutor.execute(testStep, node)
                    } else if (testStep.event != null) {
                        eventTaskExecutor.execute(testStep, node)
                    } else null
                }
            }
            .toMap()

        val results = runTasksAsyncAndWait(tasks,{ isValid(testStep, refineAndGetResult(it, testStep)) },
            testStep.delay, testStep.attempts, testStep.timeout)
        return refineAndGetResult(results, testStep)
    }

    private fun getNode(node: String): Web3j =
        web3Nodes[node] ?: error("Node '$node' was not registered in Web3 Processor. Registered nodes are: ${web3Nodes.keys}")

    private fun refineAndGetResult(results: Map<*, Any?>, testStep: Web3TestStep) =
        if (isResultForSingleService(results, testStep)) results.values.first() else results

    private fun isResultForSingleService(results: Map<*, Any?>, testStep: Web3TestStep) =
        // when only one node is registered and test step doesn't have node name then return non-grouped result
        results.size == 1 && web3Nodes.size == 1 && testStep.nodes.isNullOrEmpty()

    override fun close() {
        web3Nodes.values.forEach { it.shutdown() }
    }

    override fun getTestStepClass(): Class<Web3TestStep> {
        return Web3TestStep::class.java
    }

    class Builder : BaseTestStepProcessor.Builder<Web3TestStep>() {

        companion object {
            private const val URL_KEYWORD = "url"
            private const val NODE_NAME_KEYWORD = "nodeName"
            private const val WEB3_CONFIG_KEYWORD = "web3_nodes"
        }

        private val web3Nodes = mutableMapOf<String, Web3j>()
        private val typeRefs = mutableMapOf<String, TypeReference<out Type<out Any>>>()

        fun withWeb3Node(serviceName: String?, url: String?) = apply {
            serviceName?.let { url?.let { web3Nodes[serviceName] = Web3j.build(HttpService(url)); } }
        }

        fun withWeb3Nodes(config: Config) = apply {
            if (config.hasPath(WEB3_CONFIG_KEYWORD)) {
                (config.getAnyRefList(WEB3_CONFIG_KEYWORD) as List<Map<String, String>>)
                    .forEach { withWeb3Node(it[NODE_NAME_KEYWORD], it[URL_KEYWORD]) }
            }
        }

        fun withTypeRef(typeName: String, typeRef: TypeReference<out Type<out Any>>) = apply {
            typeRefs[typeName] = typeRef
        }

        override fun build(): TestStepProcessor<Web3TestStep> {
            if (web3Nodes.isEmpty()) {
                throw TestDataProcessingInitException("No Web3 nodes were registered for the processor")
            }
            return Web3TestStepProcessor(web3Nodes, testScenarioState!!, validator!!, testStepResultConverter, typeRefs)
        }

    }
}