package org.skellig.performance.runner.service

import io.prometheus.client.exporter.MetricsServlet
import org.skellig.teststep.runner.context.SkelligTestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean


@SpringBootApplication
abstract class SkelligPerformanceServiceRunner {

    protected abstract fun getTestSteps(): Array<String>

    protected abstract fun getConfigFileName(): String

    protected abstract fun getContext(): SkelligTestContext

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
    open fun context(): SkelligTestContext = getContext()

    @Bean
    open fun testSteps(): List<String> {
        return getTestSteps().toList()
    }

    @Bean
    open fun configPath(): String {
        return getConfigName(getConfigFileName())
    }

    @Bean
    open fun servletRegistrationBean(): ServletRegistrationBean<MetricsServlet> {
        return ServletRegistrationBean(MetricsServlet(), true, "/metrics")
    }
}