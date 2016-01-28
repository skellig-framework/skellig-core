package org.skellig.teststep.processing.valueextractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultValueExtractor implements TestStepValueExtractor {

    private static final Pattern EXTRACTION_PARAMETER_PATTERN = Pattern.compile("([\\w_-]+)\\((.+)\\)|\\((.+)\\)");

    private Collection<TestStepValueExtractor> valueExtractors;

    protected DefaultValueExtractor(Collection<TestStepValueExtractor> valueExtractors) {
        this.valueExtractors = valueExtractors;
    }

    @Override
    public Object extract(Object value, String extractionParameter) {
        Matcher matcher = EXTRACTION_PARAMETER_PATTERN.matcher(extractionParameter);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            return extract(functionName, value, getExtractionParameter(matcher));
        } else {
            return value;
        }
    }

    private Object extract(String extractFunctionName, Object value, String parameter) {
        return valueExtractors.stream()
                .filter(valueExtractor -> valueExtractor.getExtractFunctionName().equals(extractFunctionName))
                .findFirst()
                .map(valueExtractor -> valueExtractor.extract(value, parameter))
                .orElse(value);
    }

    @Override
    public String getExtractFunctionName() {
        return null;
    }

    private String getExtractionParameter(Matcher matcher) {
        return matcher.groupCount() == 3 ? matcher.group(3) : matcher.group(2);
    }

    public static class Builder {

        private Collection<TestStepValueExtractor> valueExtractors;

        public Builder() {
            valueExtractors = new ArrayList<TestStepValueExtractor>() {
                {
                    add(new JsonPathTestStepValueExtractor());
                    add(new XPathTestStepValueExtractor());
                    add(new ObjectTestStepValueExtractor());
                    add(new RegexTestStepValueExtractor());
                }
            };
        }

        public Builder withValueExtractors(TestStepValueExtractor valueExtractor) {
            this.valueExtractors.add(valueExtractor);
            return this;
        }

        public TestStepValueExtractor build() {
            return new DefaultValueExtractor(valueExtractors);
        }
    }
}
