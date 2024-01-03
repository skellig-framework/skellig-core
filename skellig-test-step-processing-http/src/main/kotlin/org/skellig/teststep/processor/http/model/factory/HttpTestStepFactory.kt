package org.skellig.teststep.processor.http.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.http.model.HttpTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

class HttpTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    defaultTestDataConverter: String? = null
) : BaseDefaultTestStepFactory<HttpTestStep>(testStepRegistry, valueExpressionContextFactory, defaultTestDataConverter) {

    companion object {
        private val SERVICE_KEYWORD = fromProperty("services")
        private val URL_KEYWORD = fromProperty("url")
        private val METHOD_KEYWORD = fromProperty("http_method")
        private val HEADERS_KEYWORD = fromProperty("http_headers")
        private val QUERY_KEYWORD = fromProperty("http_query")
        private val FORM_KEYWORD = fromProperty("form")
        private val USER_KEYWORD = fromProperty("username")
        private val PASSWORD_KEYWORD = fromProperty("password")
    }

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<HttpTestStep> {
        val services = getStringArrayDataFromRawTestStep(SERVICE_KEYWORD, rawTestStep, parameters)
        return HttpTestStep.Builder()
            .withService(services)
            .withUrl(convertValue<String>(rawTestStep[URL_KEYWORD], parameters))
            .withMethod(convertValue<String>(rawTestStep[METHOD_KEYWORD], parameters))
            .withHeaders(getHttpHeaders(rawTestStep, parameters))
            .withQuery(getHttpQuery(rawTestStep, parameters))
            .withForm(getForm(rawTestStep, parameters))
            .withUsername(convertValue<String>(rawTestStep[USER_KEYWORD], parameters))
            .withPassword(convertValue<String>(rawTestStep[PASSWORD_KEYWORD], parameters))
    }

    private fun getForm(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Map<String, String?>? {
        return convertValue<Map<String, String?>>(rawTestStep[FORM_KEYWORD], parameters)
    }

    private fun getHttpQuery(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Map<String, String?>? {
        return convertValue<Map<String, String?>>(rawTestStep[QUERY_KEYWORD], parameters)
    }

    private fun getHttpHeaders(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Map<String, String?>? {
        return convertValue<Map<String, String?>>(rawTestStep[HEADERS_KEYWORD], parameters)
    }


    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(URL_KEYWORD) && rawTestStep.containsKey(METHOD_KEYWORD)
    }
}