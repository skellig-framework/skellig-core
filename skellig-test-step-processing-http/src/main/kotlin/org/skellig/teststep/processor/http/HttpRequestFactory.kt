package org.skellig.teststep.processor.http

import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.skellig.teststep.processor.http.model.HttpMethodName
import org.skellig.teststep.processor.http.model.HttpRequestDetails


class HttpRequestFactory(private val baseUrl: String?) {

    fun createRequest(httpRequestDetails: HttpRequestDetails): Request {
        val builder = Request.Builder()
            .url(createUrl(httpRequestDetails))
            .method(httpRequestDetails.verb.toString(), getRequestBody(httpRequestDetails))

        if (httpRequestDetails.formParams.isNotEmpty()) {
            val formBody = FormBody.Builder()
            httpRequestDetails.formParams.forEach { formBody.add(it.key, it.value ?: "") }
            builder.post(formBody.build())
        }

        httpRequestDetails.username?.let {
            builder.header("Authorization", Credentials.basic(httpRequestDetails.username, httpRequestDetails.password ?: ""))
        }
        httpRequestDetails.headers.forEach { builder.header(it.key, it.value ?: "") }

        return builder.build()
    }

    private fun getRequestBody(httpRequestDetails: HttpRequestDetails): RequestBody? {
        val mediaType = (httpRequestDetails.headers["Content-Type"] ?: "application/json").toMediaType()
        val requestBody = httpRequestDetails.body?.toRequestBody(mediaType)
        return if (httpRequestDetails.verb == HttpMethodName.POST) requestBody ?: "".toRequestBody(mediaType)
        else requestBody
    }

    private fun createUrl(httpRequest: HttpRequestDetails): String {
        val urlBuilder = (baseUrl + httpRequest.url).toHttpUrl().newBuilder()

        return if (httpRequest.queryParams.isEmpty()) {
            urlBuilder.build().toString()
        } else {
            httpRequest.queryParams.entries.forEach { urlBuilder.addQueryParameter(it.key, it.value) }
            return urlBuilder.build().toString()
        }
    }
}