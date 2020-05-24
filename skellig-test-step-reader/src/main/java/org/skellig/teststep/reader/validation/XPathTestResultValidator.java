package org.skellig.teststep.reader.validation;

import org.apache.commons.lang3.StringUtils;
import org.skellig.teststep.reader.exception.ValidationException;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPathTestResultValidator extends BaseTestResultValidator {

    @Override
    protected void validateActualResult(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult) {
        Document xml = parseToXmlDocument((String) actualResult);

        expectedResult.getActualExpectedValues().entrySet().stream()
                .filter(entry -> !StringUtils.isEmpty(entry.getKey()))
                .forEach(entry -> compareXPathValueWithExpected(xml, entry.getKey(), entry.getValue(), actualResult));
    }

    private void compareXPathValueWithExpected(Document document, String actualXPathValue, Object expectedValue,
                                               Object originalTestResult) {
        String xpath = extractXPath(actualXPathValue, ValidationType.XPATH.getPattern());
        Object actualValue = extractDataFromXpath(document, xpath);
        if (!valueComparator.compare(actualValue, expectedValue)) {
            String errorMessage =
                    String.format("Failed to validate test result '%s' in xpath '%s'. Expected: '%s', actual: '%s'",
                            originalTestResult, xpath, expectedValue, actualValue);
            throw new ValidationException(errorMessage);
        }
    }

    private Document parseToXmlDocument(String xml) {
        try (StringReader xmlReader = new StringReader(xml)) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(xmlReader));
        } catch (Exception ex) {
            throw new ValidationException("Cannot parse XML document for validation: " + xml, ex);
        }
    }

    private Object extractDataFromXpath(Document document, String xpath) {
        try {
            XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
            return String.valueOf(xPathExpression.evaluate(document, XPathConstants.STRING)).trim();
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    private String extractXPath(String key, Pattern pattern) {
        Matcher matcher = pattern.matcher(key);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return key;
    }

    @Override
    public boolean isApplicableFor(ValidationType validationType) {
        return ValidationType.XPATH.equals(validationType);
    }
}
