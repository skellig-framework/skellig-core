package org.skellig.teststep.processor.http

import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.NameValuePair
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.*
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processor.http.model.HttpMethodName
import org.skellig.teststep.processor.http.model.HttpRequestDetails
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

class HttpRequestFactory(private val baseUrl: String?) {

    fun createRequest(httpRequestDetails: HttpRequestDetails): HttpUriRequest {
        val request: HttpRequestBase
        try {
            request = createHttpRequest(httpRequestDetails, baseUrl + createUrl(httpRequestDetails))
            request.config = RequestConfig.custom()
                    .setSocketTimeout(httpRequestDetails.timeout)
                    .setConnectTimeout(httpRequestDetails.timeout)
                    .setConnectionRequestTimeout(httpRequestDetails.timeout)
                    .build()
            httpRequestDetails.headers.forEach { request.addHeader(it.key, it.value) }
        } catch (e: UnsupportedEncodingException) {
            throw TestStepProcessingException(e.message)
        }
        return request
    }

    @Throws(UnsupportedEncodingException::class)
    private fun createHttpRequest(httpRequestDetails: HttpRequestDetails, url: String): HttpRequestBase {
        return when (httpRequestDetails.verb) {
            HttpMethodName.GET -> HttpGet(url)
            HttpMethodName.DELETE -> HttpDelete(url)
            HttpMethodName.POST -> createRequestWithEntity(httpRequestDetails, HttpPost(url))
            HttpMethodName.PUT -> createRequestWithEntity(httpRequestDetails, HttpPut(url))
        }
    }

    private fun createUrl(httpRequest: HttpRequestDetails): String {
        return if (httpRequest.queryParams.isEmpty()) {
            httpRequest.url
        } else {
            val parameters = httpRequest.queryParams.entries
                    .map { BasicNameValuePair(it.key, it.value) }
                    .toList()
            httpRequest.url + "?" + URLEncodedUtils.format(parameters, StandardCharsets.UTF_8)
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun createRequestWithEntity(httpRequestDetails: HttpRequestDetails, request: HttpEntityEnclosingRequest): HttpRequestBase {
        if (httpRequestDetails.body != null) {
            request.entity = StringEntity(httpRequestDetails.body)
        } else if (httpRequestDetails.formParams.isNotEmpty()) {
            val params = mutableListOf<NameValuePair>()
            httpRequestDetails.formParams.forEach { params.add(BasicNameValuePair(it.key, it.value)) }
            request.entity = UrlEncodedFormEntity(params)
        }
        return request as HttpRequestBase
    }
}