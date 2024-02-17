package org.skellig.performance.runner.service

import org.skellig.teststep.runner.context.SkelligTestContext
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
abstract class SkelligPerformanceServiceRunner {

    protected abstract fun getTestSteps(): Array<String>

    protected abstract fun getConfigFileName(): String

    private fun getConfigName(config: String): String {
        val key = config.substringAfter("\${").substringBefore("}")
        return if (key.isNotEmpty()) {
            val property = System.getProperty(key, "")
            config.replace("\${$key}", property)
        } else {
            config
        }
    }

    @Bean
    open fun context(): SkelligTestContext = SkelligTestContext()

    @Bean
    open fun testSteps(): List<String> {
        return getTestSteps().toList()
    }

    @Bean
    open fun configPath(): String {
        return getConfigName(getConfigFileName())
    }

}