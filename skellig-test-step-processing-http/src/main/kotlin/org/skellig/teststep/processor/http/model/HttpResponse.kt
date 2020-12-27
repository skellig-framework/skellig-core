package org.skellig.teststep.processor.http.model

class HttpResponse(val statusCode: Int,
                   val headers: Map<String, String>,
                   val body: String?) {

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