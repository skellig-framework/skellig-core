package org.skellig.teststep.processing.valueextractor;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

class XPathTestStepValueExtractor implements TestStepValueExtractor {

    @Override
    public Object extract(Object value, String extractionParameter) {
        try (StringReader xmlReader = new StringReader((String) value)) {
            Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(xmlReader));
            return extractDataFromXpath(xml, extractionParameter);
        } catch (Exception ex) {
            return null;
        }
    }

    private String extractDataFromXpath(Document document, String xpath) throws Exception {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        return String.valueOf(xPathExpression.evaluate(document, XPathConstants.STRING)).trim();
    }

    @Override
    public String getExtractFunctionName() {
        return "xpath";
    }

}
