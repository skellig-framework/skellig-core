package org.skellig.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestStep {

    private String name;
    private Map<String, Object> parameters;

    protected TestStep(String name, Map<String, Object> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Optional<Map<String, Object>> getParameters() {
        return Optional.ofNullable(parameters == null ? null : Collections.unmodifiableMap(parameters));
    }

    public static class Builder {
        private String name;
        private Map<String, Object> parameters;

        public Builder withName(String name) {
            this.name = name.trim();
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
    }
}
