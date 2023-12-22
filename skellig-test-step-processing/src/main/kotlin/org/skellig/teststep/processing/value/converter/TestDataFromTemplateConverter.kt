package org.skellig.teststep.processing.value.converter

import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.io.StringWriter
import java.net.URL

class TestDataFromTemplateConverter(val classLoader: ClassLoader) : TestDataConverter {

    companion object {
        private const val NAME = "fromTemplate"
        private const val FILE = "file"
        private const val DATA = "data"
    }

    private var templateProvider = TemplateProvider(classLoader)

    override fun convert(data: Any?): Any? {
        return (data as? Map<*, *>)?.let {
            if (it.containsKey(NAME)) constructFromTemplate(it[NAME] as Map<*, *>)
            else constructFromTemplate(it)
        }
    }

    override fun getName(): String = NAME

    override fun isApplicable(data: Any?): Boolean {
        return data is Map<*, *> && data.size == 1 && data.containsKey(NAME) && data[NAME] is Map<*, *>
    }

    private fun constructFromTemplate(data: Map<*, *>): Any {
        if (data.containsKey(FILE) && data.containsKey(DATA)) {
            return constructFromTemplate(templateProvider.getTemplate(data[FILE].toString()), data[DATA])
        } else throw TestDataConversionException("Failed to find mandatory properties 'file' and 'data' for the data converter '$NAME' in: $data")
    }

    private fun constructFromTemplate(template: Template, dataModel: Any?): Any {
        try {
            StringWriter().use { outMessage ->
                template.process(dataModel, outMessage)
                return outMessage.toString()
            }
        } catch (e: Exception) {
            throw TestDataConversionException("Can't process template file", e)
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
                        return classLoader.getResource(filePath) ?: error("Failed to find resource at the path '$filePath'")
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