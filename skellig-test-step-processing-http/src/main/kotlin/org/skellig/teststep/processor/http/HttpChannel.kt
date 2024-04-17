package org.skellig.teststep.processor.http

import okhttp3.OkHttpClient
import okhttp3.Response
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.http.model.HttpRequestDetails
import org.skellig.teststep.processor.http.model.HttpResponse
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Represents an HTTP channel used for sending HTTP requests and receiving HTTP responses.
 *
 * @property baseUrl The base URL of the HTTP channel (optional). It is prefixed with this [url][org.skellig.teststep.processor.http.model.HttpTestStep.url].
 * @property defaultTimeoutMs The default timeout for the HTTP channel in milliseconds.
 */
open class HttpChannel(baseUrl: String, defaultTimeoutMs: Long) {

    private var httpRequestFactory = HttpRequestFactory(baseUrl)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(defaultTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(defaultTimeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(defaultTimeoutMs, TimeUnit.MILLISECONDS)
        .build()

    constructor() : this("", 30000)

    /**
     * Sends an HTTP request and returns the response.
     *
     * @param request The details of the HTTP request to be sent.
     * @return The HTTP response received from the server.
     * @throws TestStepProcessingException if there is an error sending the HTTP request.
     */
    open fun send(request: HttpRequestDetails?): HttpResponse? {
        return request?.let {
            val httpClient = getHttpClient(request)

            try {
                val httpRequest = httpRequestFactory.createRequest(request)
                val response = httpClient.newCall(httpRequest).execute()
                convertToLocalResponse(response)
            } catch (e: Exception) {
                throw TestStepProcessingException("Failed to send HTTP request to " + request.url, e)
            }
        }
    }

    private fun getHttpClient(request: HttpRequestDetails): OkHttpClient {
        return if (request.timeout > 0 && httpClient.readTimeoutMillis != request.timeout)
            httpClient.newBuilder()
                .connectTimeout(request.timeout.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(request.timeout.toLong(), TimeUnit.MILLISECONDS)
                .writeTimeout(request.timeout.toLong(), TimeUnit.MILLISECONDS)
                .build()
        else httpClient
    }

    @Throws(IOException::class)
    private fun convertToLocalResponse(response: Response): HttpResponse {
        val builder = HttpResponse.Builder().withStatusCode(response.code)
        builder.withHeaders(response.headers.associate { it.first to it.second })
        response.body?.let {
            builder.withBody(it.string())
        }

        return builder.build()
    }

    fun close() {
        httpClient.connectionPool.evictAll();
        httpClient.dispatcher.executorService.shutdown();
    }
}