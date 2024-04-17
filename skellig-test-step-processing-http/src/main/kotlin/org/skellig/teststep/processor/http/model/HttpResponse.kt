package org.skellig.teststep.processor.http.model

/**
 * HttpResponse represents an HTTP response received from a server.
 *
 * @property statusCode The HTTP status code of the response.
 * @property headers The headers of the response represented as a map.
 * @property body The body of the response represented as a string.
 */
class HttpResponse(val statusCode: Int,
                   val headers: Map<String, String>,
                   val body: String?) {

    override fun toString(): String {
        return "statusCode: $statusCode" +
                (if (headers.isNotEmpty()) ("\nheaders: $headers") else "") +
                (body?.let { ("\nbody: $body") }?:"")
    }

    class Builder {

        private var statusCode = 0
        private var headers = mapOf<String, String>()
        private var body: String? = null

        fun withBody(body: String?) = apply {
            this.body = body
        }

        fun withHeaders(theHeaders: Map<String, String>) = apply {
            headers = theHeaders
        }

        fun withStatusCode(code: Int) = apply {
            statusCode = code
        }

        fun build(): HttpResponse {
            return HttpResponse(statusCode, headers, body)
        }
    }
}