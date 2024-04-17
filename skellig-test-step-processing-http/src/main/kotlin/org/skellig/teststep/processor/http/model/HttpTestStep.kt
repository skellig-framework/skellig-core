package org.skellig.teststep.processor.http.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processing.util.PropertyFormatUtils
import org.skellig.teststep.processing.util.PropertyFormatUtils.Companion.createIndent

/**
 * Represents an HTTP test step used to execute HTTP requests from [HttpTestStepProcessor][org.skellig.teststep.processor.http.HttpTestStepProcessor].
 *
 * @param id The unique identifier of the test step
 * @param name The name of the test step
 * @param execution The type of test step execution
 * @param timeout The timeout duration for the test step
 * @param delay The delay duration before executing the test step
 * @param attempts The number of attempts to execute the test step
 * @param values The map of variable names and their corresponding values
 * @param testData The test data for the test step
 * @param validationDetails The validation details for the test step
 * @param scenarioStateUpdaters The list of scenario state updaters for the test step
 * @param url The URL for the HTTP request
 * @param services The collection of services for the test step
 * @param method The HTTP method to be used for the request
 * @param username The username for authentication
 * @param password The password for authentication
 * @param headers The map of request headers
 * @param query The map of query parameters
 * @param form The map of form data
 */
class HttpTestStep(
    id: String?,
    name: String,
    execution: TestStepExecutionType?,
    timeout: Int,
    delay: Int,
    attempts: Int,
    values: Map<String, Any?>?,
    testData: Any?,
    validationDetails: ValidationNode?,
    scenarioStateUpdaters: List<ScenarioStateUpdater>?,
    val url: String?,
    val services: Collection<String>?,
    val method: String?,
    val username: String?,
    val password: String?,
    val headers: Map<String, String?>?,
    val query: Map<String, String?>?,
    val form: Map<String, String?>?
) : DefaultTestStep(id, name, execution, timeout, delay, attempts, values, testData, validationDetails, scenarioStateUpdaters) {

    override fun toString(): String {
        return super.toString() +
                "services = $services\n" +
                "url = $url\n" +
                "method = $method\n" +
                (username?.let { "username = $username\n" } ?: "") +
                (password?.let { "password = $password\n" } ?: "") +
                headers?.let { it.entries.joinToString("\n", "headers {\n", "\n}\n") { n -> "${createIndent(1)}$n" } } +
                query?.let { it.entries.joinToString("\n", "query {\n", "\n}\n") { n -> "${createIndent(1)}$n" } } +
                form?.let { it.entries.joinToString("\n", "form {\n", "\n}\n") { n -> "${createIndent(1)}$n" } }
    }

    class Builder : DefaultTestStep.Builder<HttpTestStep>() {

        private var services: Collection<String>? = emptyList()
        private var url: String? = null
        private var method: String? = null
        private var username: String? = null
        private var password: String? = null
        private var headers: Map<String, String?>? = null
        private var query: Map<String, String?>? = null
        private var form: Map<String, String?>? = null

        fun withService(services: Collection<String>?) = apply {
            this.services = services
        }

        fun withUrl(url: String?) = apply {
            this.url = url
        }

        fun withMethod(method: String?) = apply {
            this.method = method
        }

        fun withUsername(username: String?) = apply {
            this.username = username
        }

        fun withPassword(password: String?) = apply {
            this.password = password
        }

        fun withHeaders(headers: Map<String, String?>?) = apply {
            this.headers = headers
        }

        fun withQuery(query: Map<String, String?>?) = apply {
            this.query = query
        }

        fun withForm(form: Map<String, String?>?) = apply {
            this.form = form
        }

        override fun build(): HttpTestStep {
            return HttpTestStep(
                id, name!!, execution, timeout, delay, attempts, values, testData, validationDetails,
                scenarioStateUpdaters, url, services, method, username, password, headers, query, form
            )
        }
    }
}