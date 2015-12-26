package org.skellig.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TestScenario {

    private Set<String> tags;
    private String name;
    private List<TestStep> steps;
    private List<Map<String, Object>> data;

    protected TestScenario(String name, List<TestStep> steps, Set<String> tags,
                           List<Map<String, Object>> data) {
        this.name = name;
        this.steps = steps;
        this.tags = tags;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public List<TestStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public Optional<Set<String>> getTags() {
        return Optional.ofNullable(tags == null ? null : Collections.unmodifiableSet(tags));
    }

    public Optional<List<Map<String, Object>>> getData() {
        return Optional.ofNullable(data == null ? null : Collections.unmodifiableList(data));
    }

    public static class Builder {
        private String name;
        private List<TestStep> steps;
        private Set<String> tags;
        private List<Map<String, Object>> data;

        public Builder() {
            steps = new ArrayList<>();
        }

        public Builder withName(String name) {
            this.name = name.trim();
            return this;
        }

        public Builder withStep(TestStep step) {
            this.steps.add(step);
            return this;
        }

        public Builder withTags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder withDataRow(Map<String, Object> dataRow) {
            if (data == null) {
                data = new ArrayList<>();
            }
            this.data.add(dataRow);
            return this;
        }

        public TestScenario build() {
            return new TestScenario(name, steps, tags, data);
        }
    }
}
