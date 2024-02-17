package org.skellig.teststep.processor.http

import okhttp3.OkHttpClient
import okhttp3.Response
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.http.model.HttpRequestDetails
import org.skellig.teststep.processor.http.model.HttpResponse
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

open class HttpChannel(baseUrl: String, defaultTimeoutMs: Long) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(HttpChannel::class.java)
    }

    private var httpRequestFactory = HttpRequestFactory(baseUrl)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(defaultTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(defaultTimeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(defaultTimeoutMs, TimeUnit.MILLISECONDS)
        .build()

    constructor() : this("", 30000)

    open fun send(request: HttpRequestDetails?): HttpResponse? {
        return request?.let {
            val httpClient = getHttpClient(request)

            try {
                val httpRequest = httpRequestFactory.createRequest(request)
                LOGGER.debug("Run HTTP request {}", request.toString())

                val response = httpClient.newCall(httpRequest).execute()
                val localResponse = convertToLocalResponse(response)

                LOGGER.debug("Received HTTP response from {}: {}", httpRequest.url, localResponse?.toString())

                localResponse
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
    private fun convertToLocalResponse(response: Response): HttpResponse? {
        val builder = HttpResponse.Builder().withStatusCode(response.code)
        builder.withHeaders(response.headers.associate { it.first to it.second })
        response.body?.let {
            builder.withBody(it.string())
        }

        return builder.build()
    }

    fun close() {
        LOGGER.info("Close all existing HTTP connections for the HTTP Test Step Processor")
        httpClient.connectionPool.evictAll();
        httpClient.dispatcher.executorService.shutdown();
    }
}