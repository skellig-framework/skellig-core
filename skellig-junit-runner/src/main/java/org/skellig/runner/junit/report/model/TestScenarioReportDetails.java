package org.skellig.runner.junit.report.model;

import java.util.List;

public class TestScenarioReportDetails {
    private String name;
    private List<TestStepReportDetails> testStepReportDetails;

    public TestScenarioReportDetails(String name, List<TestStepReportDetails> testStepReportDetails) {
        this.name = name;
        this.testStepReportDetails = testStepReportDetails;
    }

    public String getName() {
        return name;
    }

    public List<TestStepReportDetails> getTestStepReportDetails() {
        return testStepReportDetails;
    }

    public int getTotalPassedTestSteps() {
        return (int) testStepReportDetails.stream()
                .filter(TestStepReportDetails::isPassed)
                .count();
    }

    public boolean isPassed() {
        return testStepReportDetails.stream().allMatch(TestStepReportDetails::isPassed);
    }

    public int getTotalFailedTestSteps() {
        return getTestStepReportDetails().size() - getTotalPassedTestSteps();
    }
}