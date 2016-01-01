package org.skellig.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TestScenario {

    private Set<String> tags;
    private String name;
    private List<TestStep> steps;

    protected TestScenario(String name, List<TestStep> steps, Set<String> tags) {
        this.name = name;
        this.steps = Collections.unmodifiableList(steps);
        this.tags = tags == null ? null : Collections.unmodifiableSet(tags);
    }

    public String getName() {
        return name;
    }

    public List<TestStep> getSteps() {
        return steps;
    }

    public Optional<Set<String>> getTags() {
        return Optional.ofNullable(tags);
    }

    public static class Builder {

        private String name;
        private Set<String> tags;
        private List<TestStep.Builder> stepBuilders;
        private List<Map<String, String>> data;

        public Builder() {
            stepBuilders = new ArrayList<>();
        }

        public Builder withName(String name) {
            this.name = name.trim();
            return this;
        }

        public Builder withStep(TestStep.Builder stepBuilder) {
            this.stepBuilders.add(stepBuilder);
            return this;
        }

        public Builder withTags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder withDataRow(Map<String, String> dataRow) {
            if (data == null) {
                data = new ArrayList<>();
            }
            this.data.add(dataRow);
            return this;
        }

        public List<TestScenario> build() {
            if (data != null) {
                return data.stream()
                        .map(testDataRow ->
                                new TestScenario(ParametersUtils.replaceParametersIfFound(name, testDataRow),
                                        getTestStepsWithAppliedTestData(testDataRow), tags))
                        .collect(Collectors.toList());
            } else {
                List<TestStep> steps = stepBuilders.stream()
                        .map(TestStep.Builder::build)
                        .collect(Collectors.toList());
                return Collections.singletonList(new TestScenario(name, steps, tags));
            }
        }

        private List<TestStep> getTestStepsWithAppliedTestData(Map<String, String> testDataRow) {
            return stepBuilders.stream()
                    .map(step -> step.buildAndApplyTestData(testDataRow))
                    .collect(Collectors.toList());
        }
    }
}
