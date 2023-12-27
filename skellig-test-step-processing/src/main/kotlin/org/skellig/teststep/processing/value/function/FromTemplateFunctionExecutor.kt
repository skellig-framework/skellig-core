package org.skellig.teststep.processing.value.function

import freemarker.cache.URLTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.io.StringWriter
import java.net.URL

class FromTemplateFunctionExecutor(val classLoader: ClassLoader) : FunctionValueExecutor {

    companion object {
        private const val FILE = "file"
        private const val DATA = "data"
    }

    private var templateProvider: TemplateProvider = TemplateProvider(classLoader)

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        return if (args.size == 2) {
            val file = args[0]?.toString()
            val templateDetails = args[1]
            constructFromTemplate(templateProvider.getTemplate(file), templateDetails)
        } else if (args.size == 1) {
            if (args[0] is Map<*,*>) {
                val file = (args[0] as Map<*, *>)[FILE]?.toString()
                    ?: throw FunctionExecutionException("The argument of the function `${getFunctionName()}` must have property '$FILE' defined")
                val data = (args[0] as Map<*, *>)[DATA]
                    ?: throw FunctionExecutionException("The argument of the function `${getFunctionName()}` must have property '$DATA' defined")
                constructFromTemplate(templateProvider.getTemplate(file), data)
            } else throw FunctionExecutionException("Invalid argument for the function `${getFunctionName()}`. Expected Map but found '${args[0]?.javaClass}'")
        } else throw FunctionExecutionException("Function `${getFunctionName()}` can only accept 1 or 2 arguments. Found ${args.size}")
    }

    private fun constructFromTemplate(template: Template, dataModel: Any?): Any {
        try {
            StringWriter().use { outMessage ->
                template.process(dataModel, outMessage)
                return outMessage.toString()
            }
        } catch (e: Exception) {
            throw FunctionExecutionException("Can't process template file", e)
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
                throw FunctionExecutionException(String.format("Failed to load template file '%s'", filePath), e)
            }
        }
    }
}