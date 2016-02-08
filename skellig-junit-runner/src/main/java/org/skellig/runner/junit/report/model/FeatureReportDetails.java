package org.skellig.runner.junit.report.model;

import java.util.List;

public class FeatureReportDetails {
    private String name;
    private List<TestScenarioReportDetails> testScenarioReportDetails;
    private int totalTestSteps = -1;
    private int totalPassedTestSteps = -1;

    public FeatureReportDetails(String name, List<TestScenarioReportDetails> testScenarioReportDetails) {
        this.name = name;
        this.testScenarioReportDetails = testScenarioReportDetails;
    }

    public String getName() {
        return name;
    }

    public List<TestScenarioReportDetails> getTestScenarioReportDetails() {
        return testScenarioReportDetails;
    }

    public int getTotalTestSteps() {
        if (totalTestSteps == -1) {
            totalTestSteps = testScenarioReportDetails.stream()
                    .map(item -> item.getTestStepReportDetails().size())
                    .reduce(0, Integer::sum);
        }
        return totalTestSteps;
    }

    public int getTotalPassedTestSteps() {
        if (totalPassedTestSteps == -1) {
            totalPassedTestSteps = testScenarioReportDetails.stream()
                    .map(TestScenarioReportDetails::getTotalPassedTestSteps)
                    .reduce(0, Integer::sum);
        }
        return totalPassedTestSteps;
    }

    public float getTotalPassedPercentage() {
        return ((float) getTotalPassedTestSteps() / getTotalTestSteps()) * 100;
    }
}