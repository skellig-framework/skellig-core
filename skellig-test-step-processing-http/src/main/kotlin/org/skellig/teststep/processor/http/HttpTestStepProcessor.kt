package org.skellig.teststep.processor.http

import com.typesafe.config.Config
import org.skellig.task.async.AsyncTaskUtils.Companion.runTasksAsyncAndWait
import org.skellig.teststep.processing.exception.TestDataProcessingInitException
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processor.http.model.HttpMethodName
import org.skellig.teststep.processor.http.model.HttpRequestDetails
import org.skellig.teststep.processor.http.model.HttpResponse
import org.skellig.teststep.processor.http.model.HttpTestStep

class HttpTestStepProcessor(
    private val httpServices: Map<String, HttpChannel>,
    testScenarioState: TestScenarioState
) : BaseTestStepProcessor<HttpTestStep>(testScenarioState) {

    private val log = logger<HttpTestStepProcessor>()

    override fun processTestStep(testStep: HttpTestStep): Any? {
        var services: Collection<String>? = testStep.services
        if (services.isNullOrEmpty()) {
            if (httpServices.size > 1) {
                throw TestStepProcessingException(
                    "No services were provided to run an HTTP request." +
                            " Registered services are: " + httpServices.keys.toString()
                )
            } else {
                services = httpServices.keys
            }
        }
        log.info(testStep, "Start to run HTTP query of test step '${testStep.name}' in $services services")

        val tasks = services.associateWith {
            {
                val request = buildHttpRequestDetails(testStep)
                log.debug(testStep) { "Run HTTP request $request" }

                val response = getHttpService(it).send(request)
                log.debug(testStep) { "Received HTTP response from ${request.url}: $response" }

                response
            }
        }
        val results = runTasksAsyncAndWait(tasks, { isValid(testStep, it) }, testStep.delay, testStep.attempts, testStep.timeout)
        return if (isResultForSingleService(results, testStep)) results.values.first() else results
    }

    private fun getHttpService(serviceName: String): HttpChannel =
        httpServices[serviceName] ?: error(
            "Service '$serviceName' was not registered in HTTP Processor." +
                    " Registered services are: ${httpServices.keys}"
        )

    private fun isResultForSingleService(results: Map<*, HttpResponse?>, testStep: HttpTestStep) =
        // when only one service is registered and test step doesn't have service name then return non-grouped result
        results.size == 1 && httpServices.size == 1 && testStep.services.isNullOrEmpty()

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

    override fun close() {
        log.info("Close Http Test Step Processor and all connections to services")
        httpServices.values.forEach { it.close() }
    }

    override fun getTestStepClass(): Class<HttpTestStep> {
        return HttpTestStep::class.java
    }

    class Builder : BaseTestStepProcessor.Builder<HttpTestStep>() {

        companion object {
            private const val URL_KEYWORD = "url"
            private const val SERVICE_NAME_KEYWORD = "serviceName"
            private const val DEFAULT_TIMEOUT_KEYWORD = "timeout"
            private const val DEFAULT_TIMEOUT_MS = 30000L
            private const val HTTP_SERVICES_KEYWORD = "http.services"
        }

        private val log = logger<Builder>()
        private val httpChannelPerService = mutableMapOf<String, HttpChannel>()


        fun withHttpService(serviceName: String?, url: String?, defaultTimeoutMs: Long) = apply {
            log.debug { "Register HTTP service '$serviceName' with default URL '$url'" }
            serviceName?.let { url?.let { httpChannelPerService[serviceName] = HttpChannel(url, defaultTimeoutMs) } }
        }

        fun withHttpService(serviceName: String?, url: String?) = withHttpService(serviceName, url, DEFAULT_TIMEOUT_MS)

        fun withHttpService(config: Config) = apply {
            if (config.hasPath(HTTP_SERVICES_KEYWORD)) {
                log.info("HTTP configuration found in the Config file. Start to register its HTTP services")
                val services = config.getAnyRef(HTTP_SERVICES_KEYWORD)
                if (services is List<*>) {
                    services.mapNotNull { it as? Map<*, *> }
                        .forEach {
                            withHttpService(
                                it[SERVICE_NAME_KEYWORD]?.toString(),
                                it[URL_KEYWORD]?.toString(),
                                it[DEFAULT_TIMEOUT_KEYWORD]?.toString()?.toLong() ?: DEFAULT_TIMEOUT_MS
                            )
                        }
                }
            }
        }

        override fun build(): TestStepProcessor<HttpTestStep> {
            if (httpChannelPerService.isEmpty()) {
                throw TestDataProcessingInitException("No HTTP services were registered for the processor")
            }
            return HttpTestStepProcessor(httpChannelPerService, testScenarioState!!)
        }

    }
}