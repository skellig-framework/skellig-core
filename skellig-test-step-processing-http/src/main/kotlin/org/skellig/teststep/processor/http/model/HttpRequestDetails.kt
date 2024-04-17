package org.skellig.teststep.processor.http.model

/**
 * Class representing the details of an HTTP request.
 *
 * @property verb The HTTP method name.
 * @property url The URL of the request.
 * @property headers The headers of the request.
 * @property formParams The form parameters of the request.
 * @property body The body of the request.
 * @property queryParams The query parameters of the request.
 * @property username The username for authentication.
 * @property password The password for authentication.
 * @property timeout The timeout for the request.
 */
class HttpRequestDetails(val verb: HttpMethodName,
                         val url: String,
                         val headers: Map<String, String?>,
                         val formParams: Map<String, String?>,
                         val body: String?,
                         val queryParams: Map<String, String?>,
                         val username: String?,
                         val password: String?,
                         val timeout: Int) {

    override fun toString(): String {
        return "$verb:$url" +
                (if (headers.isNotEmpty()) ("\nheaders: $headers") else "") +
                (if (formParams.isNotEmpty()) ("\nformParams: $formParams") else "") +
                (if (queryParams.isNotEmpty()) ("\nqueryParams: $queryParams") else "") +
                (body?.let { ("\nbody: $body") } ?: "") +
                (username?.let { ("\nusername: $username") } ?: "") +
                (password?.let { ("\npassword: $password") } ?: "")
    }

    class Builder(private val verb: HttpMethodName) {

        private var url: String? = null
        private var headers = emptyMap<String, String?>()
        private var formParams = emptyMap<String, String?>()
        private var queryParams = emptyMap<String, String?>()
        private var body: String? = null
        private var username: String? = null
        private var password: String? = null
        private var timeout = 0

        fun withUrl(url: String?) = apply {
            this.url = url
        }

        fun withHeaders(headers: Map<String, String?>?) = apply {
            if (headers != null) {
                this.headers = headers
            }
        }

        fun withFormParam(formParams: Map<String, String?>?) = apply {
            if (formParams != null) {
                this.formParams = formParams
            }
        }

        fun withQueryParam(queryParams: Map<String, String?>?) = apply {
            if (queryParams != null) {
                this.queryParams = queryParams
            }
        }

        fun withBody(body: String?) = apply {
            this.body = body
        }

        fun withUsername(username: String?) = apply {
            this.username = username
        }

        fun withPassword(password: String?) = apply {
            this.password = password
        }

        fun withTimeout(timeout: Int) = apply {
            this.timeout = timeout
        }

        fun build(): HttpRequestDetails {
            return HttpRequestDetails(verb, url!!, headers, formParams, body, queryParams, username, password, timeout)
        }
    }

}