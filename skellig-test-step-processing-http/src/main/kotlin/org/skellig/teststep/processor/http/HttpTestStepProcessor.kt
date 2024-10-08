package org.skellig.teststep.processor.http

import com.typesafe.config.Config
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.exception.TestStepProcessorInitException
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
import java.util.concurrent.TimeoutException

/**
 * The HttpTestStepProcessor class is responsible for processing HTTP test steps
 * in a test scenario. It sends HTTP requests to the specified services and
 * returns the responses received.
 *
 * The HTTP request runs in parallel on each service defined in [HttpTestStep]. After it gets result from all parallel runs,
 * it's validated based on details provided in [HttpTestStep.validationDetails] and if not valid, it will retry the
 * same processing with N-[attempts][HttpTestStep.attempts] (if more than 0).
 *
 * Each individual run of HTTP request per service, can be limited by a [timeout][HttpTestStep.timeout].
 *
 * The result of test step processing is a Map where key is a service name from [services][HttpTestStep.services] and value
 * is a [HttpResponse] from execution of the request. If only 1 service name is provided in [services][HttpTestStep.services], then
 * the result is a single [HttpResponse] from execution of the request.
 *
 * @param httpServices A map containing the registered HTTP channels. The key represents the service name from [HttpTestStep.services]
 * and the [HttpChannel] value represents the HTTP channel used for sending requests.
 */
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
        log.info(testStep, "Start to run HTTP request of test step '${testStep.name}' in $services services")

        val tasks = services.associateWith {
            {
                val request = buildHttpRequestDetails(testStep)
                log.info(testStep, "Run HTTP request on service '$it': $request")

                try {
                    val response = getHttpService(it).send(request)
                    log.info(testStep, "Received HTTP response from $it[${request.url}]:\n$response")
                    response
                } catch (ex: TestStepProcessingException) {
                    log.warn("Return a null response because the service '$it' fails to get the result from url '${request.url}' due to: ${ex.cause?.message}")
                    null
                }
            }
        }
        val results = runTasksAsyncAndWait(tasks, testStep)
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
        return HttpRequestDetails.Builder(HttpMethodName.valueOf(httpTestStep.method))
            .withTimeout(httpTestStep.timeout)
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
                throw TestStepProcessorInitException("No HTTP services were registered for the processor")
            }
            return HttpTestStepProcessor(httpChannelPerService, testScenarioState!!)
        }

    }
}