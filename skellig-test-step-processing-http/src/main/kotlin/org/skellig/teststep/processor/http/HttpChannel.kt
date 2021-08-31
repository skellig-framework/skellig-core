package org.skellig.teststep.processor.http

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.http.model.HttpRequestDetails
import org.skellig.teststep.processor.http.model.HttpResponse
import org.slf4j.LoggerFactory
import java.io.IOException

open class HttpChannel(baseUrl: String) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(HttpChannel::class.java)
    }

    private var httpRequestFactory = HttpRequestFactory(baseUrl)

    constructor() : this("")

    open fun send(request: HttpRequestDetails?): HttpResponse? {
        return request?.let {
            val httpClientBuilder = HttpClientBuilder.create()
            request.username?.let {
                authoriseRequest(request.username, request.password ?: "", httpClientBuilder)
            }

            try {
                httpClientBuilder.build().use { httpClient ->
                    val httpRequest = createHttpRequest(request)
                    LOGGER.debug("Run HTTP request {}", request.toString())

                    val response: org.apache.http.HttpResponse = httpClient.execute(httpRequest)

                    val localResponse = convertToLocalResponse(response)

                    LOGGER.info("Received HTTP response from {}: {}", httpRequest.uri, localResponse.toString())

                    localResponse
                }
            } catch (e: Exception) {
                throw TestStepProcessingException("Failed to send HTTP request to " + request.url, e)
            }
        }
    }

    private fun authoriseRequest(userName: String, password: String, httpClientBuilder: HttpClientBuilder) {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(userName, password))
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
    }

    private fun createHttpRequest(httpRequestDetails: HttpRequestDetails): HttpUriRequest {
        val request = httpRequestFactory.createRequest(httpRequestDetails)
        httpRequestDetails.headers.forEach { (s: String?, s1: String?) -> request.addHeader(s, s1) }

        return request
    }

    @Throws(IOException::class)
    private fun convertToLocalResponse(response: org.apache.http.HttpResponse): HttpResponse? {
        val builder = HttpResponse.Builder().withStatusCode(response.statusLine.statusCode)
        builder.withHeaders(response.allHeaders.map { it.name to it.value }.toMap())
        if (response.entity != null) {
            builder.withBody(EntityUtils.toString(response.entity))
        }
        return builder.build()
    }
}