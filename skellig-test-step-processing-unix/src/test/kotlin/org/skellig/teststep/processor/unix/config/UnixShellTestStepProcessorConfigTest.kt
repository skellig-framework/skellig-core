package org.skellig.teststep.processor.unix.config

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.state.DefaultTestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.processor.unix.DefaultSshClient
import org.skellig.teststep.processor.unix.UnixShellTestStepProcessor
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.alphaNum
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.list
import org.skellig.teststep.reader.value.expression.ValueExpressionObject.map

class UnixShellTestStepProcessorConfigTest {


    @Test
    fun `config unix shell processor where config not defined`() {
        val config = UnixShellTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(mock(), ConfigFactory.load("empty-unix-test.conf"), mock(), mock(), mock(), mock())
        )
        assertNull(config, "Config should not be defined")
    }

    @Test
    fun `config unix shell processor`() {
        val state = DefaultTestScenarioState()
        val config = UnixShellTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state, ConfigFactory.load("unix-test.conf"), mock(),
                ValueExpressionContextFactory(
                    DefaultFunctionValueExecutor.Builder()
                        .withTestScenarioState(state)
                        .withClassLoader(this.javaClass.classLoader)
                        .build(), DefaultPropertyExtractor(null)
                ), mock(), mock()
            )
        )
        val processor = config!!.testStepProcessor as UnixShellTestStepProcessor

        assertAll(
            { assertEquals(2, processor.hosts.size) },
            { assertEquals("localhost", (processor.hosts["srv1"] as DefaultSshClient).host) },
            { assertEquals(1010, (processor.hosts["srv1"] as DefaultSshClient).port) },
            { assertEquals("~/.ssh/id_rsa", (processor.hosts["srv1"] as DefaultSshClient).publicSshKeyPath) },
            { assertEquals("1.2.3.4", (processor.hosts["srv2"] as DefaultSshClient).host) },
            { assertEquals("usr1", (processor.hosts["srv2"] as DefaultSshClient).user) },
            { assertEquals("pswd1", (processor.hosts["srv2"] as DefaultSshClient).password) }
        )
    }

    @Test
    fun `config unix shell test step factory`() {
        val state = DefaultTestScenarioState()
        val config = UnixShellTestStepProcessorConfig().config(
            TestStepProcessorConfigDetails(
                state, ConfigFactory.load("unix-test.conf"), mock(),
                ValueExpressionContextFactory(
                    DefaultFunctionValueExecutor.Builder()
                        .withTestScenarioState(state)
                        .withClassLoader(this.javaClass.classLoader)
                        .build(), DefaultPropertyExtractor(null)
                ), mock(), mock()
            )
        )
        val testStepFactory = config!!.testStepFactory

        val rawTestStep = mapOf<ValueExpression, ValueExpression>(
            Pair(alphaNum("hosts"), list(alphaNum("h1"))),
            Pair(alphaNum("command"), alphaNum("cmd")),
            Pair(alphaNum("args"), map(Pair(alphaNum("a"), alphaNum("b"))))
        )

        assertTrue(
            testStepFactory.isConstructableFrom(
                rawTestStep
            )
        )

        val testStep = testStepFactory.create("t1", rawTestStep, mapOf())

        assertAll(
            { assertEquals("t1", testStep.name) },
            { assertEquals(listOf("h1"), testStep.hosts) },
            { assertEquals("cmd -a b", testStep.getCommand()) },
        )
    }
}