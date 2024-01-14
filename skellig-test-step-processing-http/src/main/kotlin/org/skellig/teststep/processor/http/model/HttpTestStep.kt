package org.skellig.teststep.processor.http.model

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ScenarioStateUpdater
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.model.ValidationNode
import org.skellig.teststep.processing.util.PropertyFormatUtils
import org.skellig.teststep.processing.util.PropertyFormatUtils.Companion.createIndent

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