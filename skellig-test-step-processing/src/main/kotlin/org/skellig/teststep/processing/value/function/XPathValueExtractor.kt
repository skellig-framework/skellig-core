package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class XPathValueExtractor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        if (args.size == 1) {
            val extractionParameter = args[0]?.toString()
            value?.let {
                try {
                    StringReader(it as String).use { xmlReader ->
                        val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(xmlReader))
                        return extractDataFromXpath(xml, extractionParameter!!)
                    }
                } catch (ex: Exception) {
                    return null
                }
            } ?: throw FunctionExecutionException("Cannot extract xpath '%s' from null value")
        } else throw FunctionExecutionException("Function `xpath` can only accept 1 String argument. Found ${args.size}")
    }

    private fun extractDataFromXpath(document: Document, xpath: String): String {
        val xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath)
        return xPathExpression.evaluate(document, XPathConstants.STRING).toString().trim { it <= ' ' }
    }

    override fun getFunctionName(): String {
        return "xpath"
    }
}