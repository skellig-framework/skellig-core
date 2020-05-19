package org.skellig.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Feature {

    private String name;
    private List<TestScenario> scenarios;
    private List<TestPreRequisites<?>> testPreRequisites;

    protected Feature(String name, List<TestScenario> scenarios, List<TestPreRequisites<?>> testPreRequisites) {
        this.name = name;
        this.scenarios = Collections.unmodifiableList(scenarios);
        this.testPreRequisites = testPreRequisites == null ? null : Collections.unmodifiableList(testPreRequisites);
    }

    public String getName() {
        return name;
    }

    public List<TestScenario> getScenarios() {
        return scenarios;
    }

    public Optional<List<TestPreRequisites<?>>> getTestPreRequisites() {
        return Optional.ofNullable(testPreRequisites);
    }

    public static class Builder {
        private String name;
        private List<TestScenario> scenarios;
        private List<TestPreRequisites<?>> testPreRequisites;

        public Builder() {
            scenarios = new ArrayList<>();
        }

        public Builder withName(String name) {
            this.name = name.trim();
            return this;
        }

        public Builder withScenarios(List<TestScenario> scenarios) {
            this.scenarios.addAll(scenarios);
            return this;
        }

        public Builder withTestPreRequisites(List<TestPreRequisites<?>> testPreRequisites) {
            this.testPreRequisites = testPreRequisites;
            return this;
        }

        public Feature build() {
            return new Feature(name, scenarios, testPreRequisites);
        }
    }
}
