package org.skellig.teststep.processor.http

import com.typesafe.config.Config
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.http.model.HttpMethodName
import org.skellig.teststep.processor.http.model.HttpRequestDetails
import org.skellig.teststep.processor.http.model.HttpTestStep

class HttpTestStepProcessor(private val httpChannelPerService: Map<String, HttpChannel>,
                            testScenarioState: TestScenarioState,
                            validator: TestStepResultValidator,
                            testStepResultConverter: TestStepResultConverter?)
    : BaseTestStepProcessor<HttpTestStep>(testScenarioState, validator, testStepResultConverter) {

    override fun processTestStep(testStep: HttpTestStep): Any {
        if (testStep.services!!.isEmpty()) {
            throw TestStepProcessingException("No services were provided to run an HTTP request." +
                    " Registered services are: " + httpChannelPerService.keys.toString())
        }
        val result: MutableMap<String, Any?> = HashMap()
        testStep.services.parallelStream()
                .forEach { serviceName: String ->
                    if (httpChannelPerService.containsKey(serviceName)) {
                        val httpChannel = httpChannelPerService[serviceName]
                        val request = buildHttpRequestDetails(testStep)
                        result[serviceName] = httpChannel!!.send(request)
                    } else {
                        throw TestStepProcessingException(String.format(
                                "Service '%s' was not registered in HTTP Processor." +
                                        " Registered services are: %s", serviceName, httpChannelPerService.keys.toString()))
                    }
                }
        return if (result.size == 1) result.values.stream().findFirst().get() else result
    }

    private fun buildHttpRequestDetails(httpTestStep: HttpTestStep): HttpRequestDetails {
        val testData = httpTestStep.testData
        return HttpRequestDetails.Builder(HttpMethodName.valueOf(httpTestStep.method!!))
                .withUrl(httpTestStep.url)
                .withHeaders(httpTestStep.headers)
                .withQueryParam(httpTestStep.query)
                .withFormParam(httpTestStep.form)
                .withUsername(httpTestStep.username)
                .withPassword(httpTestStep.password)
                .withBody(testData?.toString())
                .build()
    }

    override fun getTestStepClass(): Class<HttpTestStep> {
        return HttpTestStep::class.java
    }

    class Builder : BaseTestStepProcessor.Builder<HttpTestStep>() {

        companion object {
            private const val URL_KEYWORD = "url"
            private const val SERVICE_NAME_KEYWORD = "serviceName"
            private const val HTTP_CONFIG_KEYWORD = "http"
        }

        private val httpChannelPerService = mutableMapOf<String, HttpChannel>()


        fun withHttpService(serviceName: String?, url: String?) = apply {
            serviceName?.let { url?.let { httpChannelPerService[serviceName] = HttpChannel(url) } }

        }

        fun withHttpService(config: Config) = apply {
            if (config.hasPath(HTTP_CONFIG_KEYWORD)) {
                (config.getAnyRefList(HTTP_CONFIG_KEYWORD) as List<Map<String, String>>)
                        .forEach { withHttpService(it[SERVICE_NAME_KEYWORD], it[URL_KEYWORD]) }
            }
        }

        override fun build(): TestStepProcessor<HttpTestStep> {
            return HttpTestStepProcessor(httpChannelPerService, testScenarioState!!, validator!!, testStepResultConverter)
        }

    }
}