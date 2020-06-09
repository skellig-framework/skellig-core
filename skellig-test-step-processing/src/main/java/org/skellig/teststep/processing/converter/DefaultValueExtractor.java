package org.skellig.teststep.processing.converter;

import java.util.ArrayList;
import java.util.Collection;

class DefaultValueExtractor {

    private Collection<TestStepValueExtractor> valueExtractors;

    DefaultValueExtractor(Collection<TestStepValueExtractor> valueExtractors) {
        this.valueExtractors = valueExtractors;
    }

    Object extract(String extractFunctionName, Object value, String parameter) {
        return valueExtractors.stream()
                .filter(valueExtractor -> valueExtractor.getExtractFunctionName().equals(extractFunctionName))
                .findFirst()
                .map(valueExtractor -> valueExtractor.extract(value, parameter))
                .orElse(value);
    }

    static class Builder {

        private Collection<TestStepValueExtractor> valueExtractors;

        public Builder() {
            valueExtractors = new ArrayList<>();
        }

        public Builder withValueExtractors(TestStepValueExtractor valueExtractor) {
            this.valueExtractors.add(valueExtractor);
            return this;
        }

        DefaultValueExtractor build() {
            return new DefaultValueExtractor(valueExtractors);
        }
    }
}
