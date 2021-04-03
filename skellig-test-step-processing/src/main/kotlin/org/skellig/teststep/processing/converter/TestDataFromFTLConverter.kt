package org.skellig.teststep.processing.converter

import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.io.StringWriter
import java.net.URL

class TestDataFromFTLConverter(val classLoader: ClassLoader,
                               val testDataFromCsvConverter: TestDataFromCsvConverter)
    : TestStepValueConverter {

    companion object {
        private const val TEMPLATE_KEYWORD = "template"
        private const val FILE_KEYWORD = "file"
    }

    private var templateProvider: TemplateProvider = TemplateProvider(classLoader)

    override fun convert(value: Any?): Any? {
        var newTestData = value
        if (newTestData is Map<*, *>) {
            val valueAsMap = newTestData as Map<String, Any?>
            if (valueAsMap.containsKey(TEMPLATE_KEYWORD)) {
                val templateDetails = valueAsMap[TEMPLATE_KEYWORD] as Map<String, Any?>
                val file = templateDetails[FILE_KEYWORD] as String?
                newTestData = constructFromTemplate(templateProvider.getTemplate(file), getDataModel(templateDetails))
            }
        }
        return newTestData
    }

    private fun constructFromTemplate(template: Template, dataModel: Map<String, *>?): Any {
        try {
            StringWriter().use { outMessage ->
                template.process(dataModel, outMessage)
                return outMessage.toString()
            }
        } catch (e: Exception) {
            throw TestDataConversionException("Can't process template file", e)
        }
    }

    private fun getDataModel(templateDetails: Map<String, Any?>): Map<String, *> {
        val dataModel = testDataFromCsvConverter.convert(templateDetails)
        return if (dataModel is List<*>) {
            (dataModel as List<Map<String, String?>>).firstOrNull() ?: mapOf<String, Any>()
        } else {
            templateDetails
        }
    }

    private class TemplateProvider(private val classLoader: ClassLoader) {
        private val templates = mutableMapOf<String?, Template>()

        fun getTemplate(relativeFilePath: String?): Template {
            return templates.computeIfAbsent(relativeFilePath) { loadFtlTemplate(relativeFilePath) }
        }

        private fun loadFtlTemplate(filePath: String?): Template {
            return try {
                val configuration = Configuration(Configuration.VERSION_2_3_30)
                configuration.templateLoader = object : URLTemplateLoader() {
                    override fun getURL(s: String): URL {
                        return classLoader.getResource(filePath)
                    }
                }
                configuration.defaultEncoding = "UTF-8"
                configuration.getTemplate("")
            } catch (e: Exception) {
                throw TestDataConversionException(String.format("Failed to load template file '%s'", filePath), e)
            }
        }
    }
}