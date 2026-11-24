package org.skellig.teststep.runner.context

import com.typesafe.config.ConfigFactory
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggingEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.processor.CompositeTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processor.scopewarning.ScopeWarningProcessor

class ScopeWarningConfigTest {
    @Test
    fun `configured parallel modes warn and none stays silent`() {
        listOf("feature", "scenario", "tag", " FEATURE ", "none", " NONE ").forEach { mode ->
            val context = object : SkelligTestContext() {
                override fun createConfig(classLoader: ClassLoader, configPath: String?) =
                    ConfigFactory.parseString("""skellig.parallel.mode = "$mode"""")
            }
            verifyWarning(context, if (mode.trim().equals("none", true)) 0 else 1)
        }
    }

    @Test
    fun `missing configuration defaults to no warning`() {
        verifyWarning(SkelligTestContext(), 0)
    }

    @Test
    fun `system property enables warnings without a config file`() {
        val key = "skellig.parallel.mode"
        val previous = System.getProperty(key)
        try {
            System.setProperty(key, "feature")
            ConfigFactory.invalidateCaches()
            verifyWarning(SkelligTestContext(), 1)
        } finally {
            if (previous == null) System.clearProperty(key) else System.setProperty(key, previous)
            ConfigFactory.invalidateCaches()
        }
    }

    private fun verifyWarning(context: SkelligTestContext, expected: Int) {
        val warnings = mutableListOf<String>()
        val logger = Logger.getLogger(CompositeTestStepProcessor::class.java)
        val previousLevel = logger.level
        val appender = object : AppenderSkeleton() {
            override fun append(event: LoggingEvent) {
                if (event.level == Level.WARN) warnings.add(event.renderedMessage)
            }
            override fun close() {}
            override fun requiresLayout() = false
        }
        logger.level = Level.WARN
        logger.addAppender(appender)
        try {
            context.use {
                val runner = it.initialize(javaClass.classLoader, listOf("scope-warning"))
                repeat(2) {
                    runner.run("Run scope warning fixture", mapOf(TestScenarioScope.PARAMETER_KEY to context.getDefaultScope()))
                        .subscribe { _, result, error ->
                            assertEquals(null, error)
                            assertEquals("done", result)
                        }
                }
            }
            assertEquals(expected, warnings.count { it.contains(ScopeWarningProcessor::class.java.name) })
        } finally {
            logger.removeAppender(appender)
            logger.level = previousLevel
        }
    }
}
