package org.skellig.runner;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.skellig.feature.Feature;
import org.skellig.feature.InitDetails;
import org.skellig.runner.exception.FeatureRunnerException;
import org.skellig.runner.junit.report.model.FeatureReportDetails;
import org.skellig.runner.tagextractor.RequestedTagExtractor;
import org.skellig.runner.tagextractor.TagExtractor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.runner.TestStepRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FeatureRunner extends ParentRunner<TestScenarioRunner> {

    private static final TagExtractor tagExtractor = new RequestedTagExtractor();

    private Feature feature;
    private List<TestScenarioRunner> testScenarioRunners;
    private TestStepRunner testStepRunner;
    private Description description;
    private TestScenarioState testScenarioState;

    protected FeatureRunner(Feature feature, TestStepRunner testStepRunner, TestScenarioState testScenarioState) throws InitializationError {
        super(feature.getClass());
        this.feature = feature;
        this.testStepRunner = testStepRunner;
        this.testScenarioState = testScenarioState;
        testScenarioRunners =
                feature.getScenarios().stream()
                        .map(testScenario -> TestScenarioRunner.create(testScenario, testStepRunner))
                        .collect(Collectors.toList());
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), feature.getName());
            getChildren().forEach(child -> description.addChild(describeChild(child)));
        }
        return description;
    }

    public FeatureReportDetails getFeatureReportDetails() {
        return new FeatureReportDetails(getName(),
                getChildren().stream()
                        .map(TestScenarioRunner::getTestScenarioReportDetails)
                        .collect(Collectors.toList()));
    }

    @Override
    protected String getName() {
        return feature.getName();
    }

    @Override
    protected List<TestScenarioRunner> getChildren() {
        return testScenarioRunners;
    }

    @Override
    protected Description describeChild(TestScenarioRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(TestScenarioRunner child, RunNotifier notifier) {
        Description childDescription = describeChild(child);
        notifier.fireTestStarted(childDescription);

        extractTagFromFeature(InitDetails.class).ifPresent(value -> testStepRunner.run(value.getId()));

        try {
            child.run(notifier);
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(childDescription, e));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(childDescription);
            testScenarioState.clean();
        }
    }

    private <T> Optional<T> extractTagFromFeature(Class<T> tagClass) {
        return tagExtractor.extract(tagClass, feature.getTestPreRequisites().orElse(Collections.emptyList()));
    }

    public static FeatureRunner create(Feature feature, TestStepRunner testStepRunner, TestScenarioState testScenarioState) {
        try {
            return new FeatureRunner(feature, testStepRunner, testScenarioState);
        } catch (InitializationError e) {
            throw new FeatureRunnerException(e.getMessage(), e);
        }
    }
}

