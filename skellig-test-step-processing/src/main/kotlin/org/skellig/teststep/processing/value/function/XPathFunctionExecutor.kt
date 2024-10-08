package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


/**
 * Executes function 'xpath' on an XML 'value'.
 *
 * Supported args:
 * - xpath(`<xpath>`) - where `<xpath>` is a xpath applied to the XML 'value' and returns the extracted data if found
 */
class XPathFunctionExecutor : FunctionValueExecutor {

    private val log = logger<XPathFunctionExecutor>()

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        if (args.size == 1) {
            val extractionParameter = args[0]?.toString() ?: error("XPath cannot be null in the function '${getFunctionName()}'")
            value?.let {
                try {
                    StringReader(it as String).use { xmlReader ->
                        val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(xmlReader))
                        return extractDataFromXpath(xml, extractionParameter)
                    }
                } catch (ex: Exception) {
                    throw FunctionExecutionException("Failed to execute function '${getFunctionName()}($extractionParameter)'", ex)
                }
            } ?: throw FunctionExecutionException("Cannot extract value by xpath '$extractionParameter' from null value")
        } else throw FunctionExecutionException("Function `${getFunctionName()}` can only accept 1 String argument. Found ${args.size}")
    }

    private fun extractDataFromXpath(document: Document, xpath: String): String {
        val xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath)
        return xPathExpression.evaluate(document, XPathConstants.STRING).toString().trim { it <= ' ' }
    }

    override fun getFunctionName(): String {
        return "xpath"
    }
}