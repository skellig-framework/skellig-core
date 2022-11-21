package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.lang.String.format
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class XPathTestStepValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
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
            } ?: throw ValueExtractionException("Cannot extract xpath '%s' from null value")
        } else throw TestDataConversionException("Function `xpath` can only accept 1 String argument. Found ${args.size}")
    }

    private fun extractDataFromXpath(document: Document, xpath: String): String {
        val xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath)
        return xPathExpression.evaluate(document, XPathConstants.STRING).toString().trim { it <= ' ' }
    }

    override fun getExtractFunctionName(): String {
        return "xpath"
    }
}