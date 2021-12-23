package org.skellig.teststep.processor.http

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.http.model.HttpRequestDetails
import org.skellig.teststep.processor.http.model.HttpResponse
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

open class HttpChannel(baseUrl: String) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(HttpChannel::class.java)
    }

    private var httpRequestFactory = HttpRequestFactory(baseUrl)
    private val cachedHttpClients = ConcurrentHashMap<String, CloseableHttpClient>()

    constructor() : this("")

    open fun send(request: HttpRequestDetails?): HttpResponse? {
        return request?.let {
            val httpClient = getHttpClient(request)

            try {
                val httpRequest = createHttpRequest(request)
                LOGGER.debug("Run HTTP request {}", request.toString())

                val response: org.apache.http.HttpResponse = httpClient.execute(httpRequest)
                val localResponse = convertToLocalResponse(response)

                LOGGER.debug("Received HTTP response from {}: {}", httpRequest.uri, localResponse.toString())

                localResponse
            } catch (e: Exception) {
                throw TestStepProcessingException("Failed to send HTTP request to " + request.url, e)
            }
        }
    }

    private fun getHttpClient(request: HttpRequestDetails): HttpClient {
        val httpClient = cachedHttpClients.computeIfAbsent(request.username ?: "") {
            val httpClientBuilder = HttpClientBuilder.create()
            request.username?.let {
                authoriseRequest(request.username, request.password ?: "", httpClientBuilder)
            }
            return@computeIfAbsent httpClientBuilder.build()
        }
        return httpClient
    }

    private fun authoriseRequest(userName: String, password: String, httpClientBuilder: HttpClientBuilder) {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(userName, password))
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
    }

    private fun createHttpRequest(httpRequestDetails: HttpRequestDetails): HttpUriRequest {
        val request = httpRequestFactory.createRequest(httpRequestDetails)
        httpRequestDetails.headers.forEach { request.addHeader(it.key, it.value) }

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

    fun close() {
        cachedHttpClients.values.forEach { it.close() }
    }
}