package org.skellig.teststep.processing.converter;

import java.util.ArrayList;
import java.util.Collection;

class DefaultValueExtractor {

    private Collection<ValueExtractor> valueExtractors;

    DefaultValueExtractor(Collection<ValueExtractor> valueExtractors) {
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

        private Collection<ValueExtractor> valueExtractors;

        public Builder() {
            valueExtractors = new ArrayList<>();
        }

        public Builder withValueExtractors(ValueExtractor valueExtractor) {
            this.valueExtractors.add(valueExtractor);
            return this;
        }

        DefaultValueExtractor build() {
            return new DefaultValueExtractor(valueExtractors);
        }
    }
}
