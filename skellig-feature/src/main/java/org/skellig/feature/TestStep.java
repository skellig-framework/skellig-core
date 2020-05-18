package org.skellig.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class TestStep {

    private String name;
    private Map<String, String> parameters;

    protected TestStep(String name, Map<String, String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Optional<Map<String, String>> getParameters() {
        return Optional.ofNullable(parameters == null ? null : Collections.unmodifiableMap(parameters));
    }

    public static class Builder {
        private String name;
        private Map<String, String> parameters;

        public Builder withName(String name) {
            this.name = name.trim();
            return this;
        }

        public Builder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withParameter(String name, String value) {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            this.parameters.put(name.trim(), value.trim());
            return this;
        }

        public TestStep build() {
            return new TestStep(name, parameters);
        }

        public TestStep buildAndApplyTestData(Map<String, String> testData) {
            Map<String, String> newParameters = getParametersWithAppliedTestData(testData);
            return new TestStep(ParametersUtils.replaceParametersIfFound(name, testData), newParameters);
        }

        private Map<String, String> getParametersWithAppliedTestData(Map<String, String> dataRow) {
            return parameters == null ? null :
                    parameters.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey,
                                    entry -> ParametersUtils.replaceParametersIfFound(entry.getValue(), dataRow)));
        }
    }
}
