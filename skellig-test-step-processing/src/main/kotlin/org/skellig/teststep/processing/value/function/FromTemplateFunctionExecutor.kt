package org.skellig.teststep.processing.value.function

import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.io.StringWriter
import java.net.URL

class FromTemplateFunctionExecutor(val classLoader: ClassLoader) : FunctionValueExecutor {

    private var templateProvider: TemplateProvider = TemplateProvider(classLoader)

    override fun execute(name: String, args: Array<Any?>): Any? {
        return if (args.size == 2) {
            val file = args[0]?.toString()
            val templateDetails = args[1]
            constructFromTemplate(templateProvider.getTemplate(file), templateDetails)
        } else throw FunctionValueExecutionException("Function `template` can only accept 2 arguments. Found ${args.size}")

    }

    private fun constructFromTemplate(template: Template, dataModel: Any?): Any {
        try {
            StringWriter().use { outMessage ->
                template.process(dataModel, outMessage)
                return outMessage.toString()
            }
        } catch (e: Exception) {
            throw FunctionValueExecutionException("Can't process template file", e)
        }
    }

    override fun getFunctionName(): String = "fromTemplate"

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
                throw FunctionValueExecutionException(String.format("Failed to load template file '%s'", filePath), e)
            }
        }
    }
}