package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.lang.String.format
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class XPathTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        value?.let {
            try {
                StringReader(it as String).use { xmlReader ->
                    val xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(xmlReader))
                    return extractDataFromXpath(xml, extractionParameter!!)
                }
            } catch (ex: Exception) {
                return null
            }
        } ?: throw ValueExtractionException(format("Cannot extract xPath '%s' from null value", extractionParameter))
    }

    @Throws(Exception::class)
    private fun extractDataFromXpath(document: Document, xpath: String): String {
        val xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath)
        return xPathExpression.evaluate(document, XPathConstants.STRING).toString().trim { it <= ' ' }
    }

    override fun getExtractFunctionName(): String? {
        return "xpath"
    }
}