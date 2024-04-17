package org.skellig.performance.runner.service

import org.skellig.teststep.runner.context.SkelligTestContext
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean


/**
 * The SkelligPerformanceServiceRunner is an abstract class that serves as a base for running performance tests,
 * and it should be extended by a concrete Spring Boot application to be able to run a serverless app which exposes
 * REST-ful controllers and HTML pages to see and run available performance tests found in the current 'resources' folder
 * of your Spring Boot application.
 */
@SpringBootApplication
abstract class SkelligPerformanceServiceRunner {

    /**
     * Paths to the test steps located in 'resources' folder of the current classpath.
     */
    protected abstract fun getTestSteps(): Array<String>

    /**
     * Returns the path to the Skellig configuration file, relative to the 'resources' folder of the current classpath.
     * The path can include env properties, enclosed in ${}, for example:
     * ```
     * /configs/skellig-${profile}.conf
     * ```
     * where 'profile' property should be provided as env argument when running the Spring Boot app, for example:
     *```
     * -Dprofile=remote
     *```
     */
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