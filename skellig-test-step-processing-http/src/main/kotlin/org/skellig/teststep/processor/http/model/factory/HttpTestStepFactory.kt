package org.skellig.teststep.processor.http.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processor.http.model.HttpTestStep
import java.util.*

class HttpTestStepFactory(keywordsProperties: Properties?,
                          testStepValueConverter: TestStepValueConverter?,
                          testDataConverter: TestDataConverter?)
    : BaseDefaultTestStepFactory<HttpTestStep>(keywordsProperties, testStepValueConverter, testDataConverter) {

    companion object {
        private const val SERVICE_KEYWORD = "test.step.keyword.services"
        private const val URL_KEYWORD = "test.step.keyword.url"
        private const val METHOD_KEYWORD = "test.step.keyword.http_method"
        private const val HEADERS_KEYWORD = "test.step.keyword.http_headers"
        private const val QUERY_KEYWORD = "test.step.keyword.http_query"
        private const val FORM_KEYWORD = "test.step.keyword.form"
        private const val USER_KEYWORD = "test.step.keyword.username"
        private const val PASSWORD_KEYWORD = "test.step.keyword.password"
    }

    protected override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<HttpTestStep> {
        val services = getStringArrayDataFromRawTestStep(getKeywordName(SERVICE_KEYWORD, "services"), rawTestStep, parameters)
        return HttpTestStep.Builder()
                .withService(services)
                .withUrl(convertValue<String>(rawTestStep[getUrlKeyword()], parameters))
                .withMethod(rawTestStep[getMethodKeyword()] as String?)
                .withHeaders(getHttpHeaders(rawTestStep, parameters))
                .withQuery(getHttpQuery(rawTestStep, parameters))
                .withForm(getForm(rawTestStep, parameters))
                .withUsername(convertValue<String>(rawTestStep[getKeywordName(USER_KEYWORD, "username")], parameters))
                .withPassword(convertValue<String>(rawTestStep[getKeywordName(PASSWORD_KEYWORD, "password")], parameters))
    }

    private fun getForm(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Map<String, String?>? {
        return (rawTestStep[getKeywordName(FORM_KEYWORD, "form")] as Map<String, String?>?)?.entries
                ?.map { it.key to convertValue<String>(it.value, parameters) }
                ?.toMap()
    }

    private fun getHttpQuery(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Map<String, String?>? {
        return (rawTestStep[getKeywordName(QUERY_KEYWORD, "http_query")] as Map<String, String?>?)?.entries
                ?.map { it.key to convertValue<String>(it.value, parameters) }
                ?.toMap()
    }

    private fun getHttpHeaders(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Map<String, String?>? {
        return (rawTestStep[getKeywordName(HEADERS_KEYWORD, "http_headers")] as Map<String, String?>?)?.entries
                ?.map { it.key to convertValue<String>(it.value, parameters) }
                ?.toMap()
    }


    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey(getUrlKeyword())
    }

    private fun getMethodKeyword(): String {
        return getKeywordName(METHOD_KEYWORD, "http_method")
    }

    private fun getUrlKeyword(): String {
        return getKeywordName(URL_KEYWORD, "url")
    }
}